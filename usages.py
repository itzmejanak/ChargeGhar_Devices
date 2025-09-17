#!/usr/bin/env python3
"""

python usages.py src/main/java/com.demo <filename>

Enhanced Usage Analysis Module for Java Codebase

This module provides sophisticated analysis of where classes, methods, and other
Java constructs are used throughout the codebase. It provides more accurate
usage detection than simple regex patterns.

Features:
- Sophisticated pattern matching for method calls
- Import statement analysis for name resolution
- Support for fully qualified names
- Detection of different usage types (instantiation, static calls, etc.)
- Caching for performance
"""

import os
import re
from collections import defaultdict
from typing import Dict, List, Set, Tuple

class UsageAnalyzer:
    """
    Advanced usage analyzer for Java codebases.

    Provides comprehensive analysis of class and method usage patterns
    with support for imports, fully qualified names, and various call patterns.
    """

    def __init__(self, base_dir: str):
        """
        Initialize the usage analyzer.

        Args:
            base_dir: Base directory to analyze
        """
        self.base_dir = base_dir
        self.java_files = self._get_java_files()
        self.import_maps = {}  # Cache for import analysis per file
        self.usage_patterns = self._build_usage_patterns()

    def _get_java_files(self) -> List[str]:
        """Recursively find all Java files in the base directory."""
        java_files = []
        for root, dirs, files in os.walk(self.base_dir):
            for file in files:
                if file.endswith('.java'):
                    java_files.append(os.path.join(root, file))
        return java_files

    def _build_usage_patterns(self) -> Dict[str, List[re.Pattern]]:
        """
        Build comprehensive regex patterns for different types of usage.

        Returns:
            Dictionary mapping usage types to lists of regex patterns
        """
        return {
            'class_instantiation': [
                # new ClassName(...)
                re.compile(r'\bnew\s+(\w+)\s*\('),
                # ClassName var = new ClassName(...)
                re.compile(r'\b(\w+)\s+\w+\s*=\s*new\s+\1\s*\('),
                # (ClassName) cast
                re.compile(r'\(\s*(\w+)\s*\)'),
            ],
            'static_method_call': [
                # ClassName.methodName(...)
                re.compile(r'\b(\w+)\s*\.\s*(\w+)\s*\('),
                # methodName(...) - when method is imported statically
                re.compile(r'\b(\w+)\s*\('),
            ],
            'instance_method_call': [
                # variable.methodName(...)
                re.compile(r'\w+\s*\.\s*(\w+)\s*\('),
            ],
            'variable_declaration': [
                # ClassName variable
                re.compile(r'\b(\w+)\s+\w+\s*[;=]'),
                # List<ClassName>, Map<String, ClassName>, etc.
                re.compile(r'<\s*(\w+)\s*>'),
                re.compile(r',\s*(\w+)\s*>'),
            ],
            'import_statements': [
                # import com.example.ClassName
                re.compile(r'import\s+([\w.]+)\s*;'),
                # import static com.example.ClassName.methodName
                re.compile(r'import\s+static\s+([\w.]+)\s*;'),
            ],
            'extends_implements': [
                # extends ClassName
                re.compile(r'extends\s+(\w+)'),
                # implements InterfaceName
                re.compile(r'implements\s+[\w\s,]*\b(\w+)\b'),
            ]
        }

    def _analyze_file_imports(self, filepath: str) -> Dict[str, str]:
        """
        Analyze import statements in a file to build a name mapping.

        Args:
            filepath: Path to the Java file

        Returns:
            Dictionary mapping short names to fully qualified names
        """
        if filepath in self.import_maps:
            return self.import_maps[filepath]

        import_map = {}

        try:
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
        except UnicodeDecodeError:
            with open(filepath, 'r', encoding='latin-1') as f:
                content = f.read()

        # Find all import statements
        import_pattern = re.compile(r'import\s+(?:static\s+)?([\w.]+)\s*;')
        for match in import_pattern.finditer(content):
            full_name = match.group(1)
            short_name = full_name.split('.')[-1]

            # Handle static imports differently
            if 'static' in match.group(0):
                # For static imports, map the method/field name to the full class
                if '.' in full_name:
                    class_name = '.'.join(full_name.split('.')[:-1])
                    member_name = full_name.split('.')[-1]
                    import_map[member_name] = f"{class_name}.{member_name}"
                else:
                    import_map[short_name] = full_name
            else:
                # Regular imports
                import_map[short_name] = full_name

        self.import_maps[filepath] = import_map
        return import_map

    def _expand_name_with_imports(self, name: str, import_map: Dict[str, str]) -> List[str]:
        """
        Expand a short name to possible fully qualified names using imports.

        Args:
            name: Short class or method name
            import_map: Import mapping for the file

        Returns:
            List of possible fully qualified names
        """
        possible_names = [name]  # Always include the original name

        if name in import_map:
            possible_names.append(import_map[name])

        # Also try common package prefixes if not found in imports
        if '.' not in name:
            possible_names.extend([
                f"java.lang.{name}",
                f"java.util.{name}",
                f"java.io.{name}",
                f"com.demo.{name}",
            ])

        return possible_names

    def find_class_usage(self, class_name: str) -> Dict[str, List[str]]:
        """
        Find where a specific class is used throughout the codebase.

        Args:
            class_name: Name of the class to search for

        Returns:
            Dictionary with file paths as keys and usage details as values
        """
        usages = defaultdict(list)

        for filepath in self.java_files:
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
            except UnicodeDecodeError:
                with open(filepath, 'r', encoding='latin-1') as f:
                    content = f.read()

            import_map = self._analyze_file_imports(filepath)
            possible_names = self._expand_name_with_imports(class_name, import_map)

            rel_path = os.path.relpath(filepath, self.base_dir)
            file_usages = []

            # Check each possible name
            for search_name in possible_names:
                short_name = search_name.split('.')[-1]

                # Class instantiation
                for pattern in self.usage_patterns['class_instantiation']:
                    for match in pattern.finditer(content):
                        if match.group(1) == short_name:
                            file_usages.append(f"Instantiated as: new {short_name}()")

                # Variable declarations
                for pattern in self.usage_patterns['variable_declaration']:
                    for match in pattern.finditer(content):
                        if match.group(1) == short_name:
                            file_usages.append(f"Used in declaration: {short_name}")

                # Extends/implements
                for pattern in self.usage_patterns['extends_implements']:
                    for match in pattern.finditer(content):
                        if match.group(1) == short_name:
                            file_usages.append(f"Inheritance: {match.group(0).strip()}")

                # Generic type parameters
                generic_pattern = re.compile(rf'<\s*[^>]*\b{re.escape(short_name)}\b[^<]*>')
                if generic_pattern.search(content):
                    file_usages.append(f"Used in generics: <{short_name}>")

            if file_usages:
                usages[rel_path] = list(set(file_usages))  # Remove duplicates

        return dict(usages)

    def find_method_usage(self, method_name: str) -> Dict[str, List[str]]:
        """
        Find where a specific method is called throughout the codebase.

        Args:
            method_name: Name of the method to search for

        Returns:
            Dictionary with file paths as keys and usage details as values
        """
        usages = defaultdict(list)

        for filepath in self.java_files:
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
            except UnicodeDecodeError:
                with open(filepath, 'r', encoding='latin-1') as f:
                    content = f.read()

            import_map = self._analyze_file_imports(filepath)
            rel_path = os.path.relpath(filepath, self.base_dir)
            file_usages = []

            # Static method calls (Class.method())
            static_pattern = re.compile(rf'\b(\w+)\s*\.\s*{re.escape(method_name)}\s*\(')
            for match in static_pattern.finditer(content):
                class_name = match.group(1)
                file_usages.append(f"Static call: {class_name}.{method_name}()")

            # Instance method calls (variable.method())
            instance_pattern = re.compile(rf'\b(\w+)\s*\.\s*{re.escape(method_name)}\s*\(')
            for match in instance_pattern.finditer(content):
                var_name = match.group(1)
                if var_name not in ['this', 'super']:  # Skip this. and super.
                    file_usages.append(f"Instance call: {var_name}.{method_name}()")

            # Direct method calls (might be static import or local method)
            direct_pattern = re.compile(rf'(?<!\.)\b{re.escape(method_name)}\s*\(')
            for match in direct_pattern.finditer(content):
                # Check if it's a static import
                if method_name in import_map:
                    file_usages.append(f"Static import call: {method_name}()")
                else:
                    file_usages.append(f"Direct call: {method_name}()")

            # Method references (Class::methodName)
            method_ref_pattern = re.compile(rf'\b(\w+)\s*::\s*{re.escape(method_name)}\b')
            for match in method_ref_pattern.finditer(content):
                class_name = match.group(1)
                file_usages.append(f"Method reference: {class_name}::{method_name}")

            if file_usages:
                usages[rel_path] = list(set(file_usages))  # Remove duplicates

        return dict(usages)

    def analyze_file(self, filename: str) -> Dict[str, any]:
        """
        Analyze a specific Java file and extract its structure.

        Args:
            filename: Name of the Java file to analyze (without .java extension)

        Returns:
            Dictionary containing file analysis results
        """
        # Find the actual file path
        target_file = None
        for filepath in self.java_files:
            if os.path.basename(filepath) == f"{filename}.java":
                target_file = filepath
                break

        if not target_file:
            return {"error": f"File {filename}.java not found"}

        try:
            with open(target_file, 'r', encoding='utf-8') as f:
                content = f.read()
        except UnicodeDecodeError:
            with open(target_file, 'r', encoding='latin-1') as f:
                content = f.read()

        # Analyze the file structure using javalang
        try:
            import javalang
            tree = javalang.parse.parse(content)
        except Exception as e:
            return {"error": f"Could not parse file: {e}"}

        analysis = {
            "filename": os.path.basename(target_file),
            "path": os.path.relpath(target_file, self.base_dir),
            "package": None,
            "imports": [],
            "classes": [],
            "methods": [],
            "fields": []
        }

        # Extract package
        for path, node in tree:
            if isinstance(node, javalang.tree.PackageDeclaration):
                analysis["package"] = node.name
                break

        # Extract imports
        for path, node in tree:
            if isinstance(node, javalang.tree.Import):
                if node.static:
                    # Handle static imports
                    if hasattr(node, 'identifier'):
                        analysis["imports"].append(f"static {node.path}.{node.identifier}")
                    else:
                        analysis["imports"].append(f"static {node.path}")
                else:
                    # Handle regular imports
                    if hasattr(node, 'identifier'):
                        analysis["imports"].append(f"{node.path}.{node.identifier}")
                    else:
                        analysis["imports"].append(f"{node.path}")

        # Extract classes and their contents
        for path, node in tree:
            if isinstance(node, javalang.tree.ClassDeclaration):
                class_info = {
                    "name": node.name,
                    "type": "class",
                    "modifiers": [m for m in node.modifiers],
                    "fields": [],
                    "methods": []
                }

                # Extract fields
                for field in node.fields:
                    for declarator in field.declarators:
                        field_info = {
                            "name": declarator.name,
                            "type": field.type.name if hasattr(field.type, 'name') else str(field.type),
                            "modifiers": [m for m in field.modifiers]
                        }
                        class_info["fields"].append(field_info)

                # Extract methods
                for method in node.methods:
                    method_info = {
                        "name": method.name,
                        "return_type": method.return_type.name if method.return_type else "void",
                        "modifiers": [m for m in method.modifiers],
                        "parameters": []
                    }

                    # Extract parameters
                    for param in method.parameters:
                        param_info = {
                            "name": param.name,
                            "type": param.type.name if hasattr(param.type, 'name') else str(param.type)
                        }
                        method_info["parameters"].append(param_info)

                    class_info["methods"].append(method_info)
                    params_str = ', '.join([f"{p['type']} {p['name']}" for p in method_info['parameters']])
                    analysis["methods"].append(f"{method_info['return_type']} {method_info['name']}({params_str})")

                analysis["classes"].append(class_info)

            elif isinstance(node, javalang.tree.InterfaceDeclaration):
                interface_info = {
                    "name": node.name,
                    "type": "interface",
                    "modifiers": [m for m in node.modifiers],
                    "methods": []
                }

                # Extract interface methods
                for method in node.methods:
                    method_info = {
                        "name": method.name,
                        "return_type": method.return_type.name if method.return_type else "void",
                        "modifiers": [m for m in method.modifiers],
                        "parameters": []
                    }

                    for param in method.parameters:
                        param_info = {
                            "name": param.name,
                            "type": param.type.name if hasattr(param.type, 'name') else str(param.type)
                        }
                        method_info["parameters"].append(param_info)

                    interface_info["methods"].append(method_info)
                    params_str = ', '.join([f"{p['type']} {p['name']}" for p in method_info['parameters']])
                    analysis["methods"].append(f"{method_info['return_type']} {method_info['name']}({params_str})")

                analysis["classes"].append(interface_info)

        return analysis

    def list_files(self) -> List[str]:
        """
        List all Java files in the codebase in a compressed format.

        Returns:
            List of relative file paths
        """
        return [os.path.relpath(filepath, self.base_dir) for filepath in self.java_files]

    def analyze_usage(self, classes: List[str], methods: List[str]) -> Dict[str, Dict[str, List[str]]]:
        """
        Analyze usage for multiple classes and methods.

        Args:
            classes: List of class names to analyze
            methods: List of method signatures to analyze

        Returns:
            Dictionary containing usage analysis for all classes and methods
        """
        print(f"Analyzing usage across {len(self.java_files)} Java files...")

        usage_results = {}

        # Analyze class usage
        for class_name in classes:
            clean_name = class_name.replace('interface ', '')
            usage_results[clean_name] = {
                'type': 'class',
                'usages': self.find_class_usage(clean_name)
            }

        # Analyze method usage
        for method_sig in methods:
            # Extract method name from signature
            method_match = re.match(r'.*\s+(\w+)\s*\(', method_sig)
            if method_match:
                method_name = method_match.group(1)
                if method_name not in usage_results:  # Avoid duplicates
                    usage_results[method_name] = {
                        'type': 'method',
                        'signature': method_sig,
                        'usages': self.find_method_usage(method_name)
                    }

        return usage_results


def find_usages(base_dir: str, classes: List[str], methods: List[str]) -> Dict[str, Dict[str, List[str]]]:
    """
    Main function to find usage of classes and methods.

    This is a backward-compatible wrapper around the UsageAnalyzer class.

    Args:
        base_dir: Base directory to search
        classes: List of class names
        methods: List of method signatures

    Returns:
        Dictionary with usage information
    """
    analyzer = UsageAnalyzer(base_dir)
    return analyzer.analyze_usage(classes, methods)


# For testing the module independently
if __name__ == '__main__':
    import sys

    if len(sys.argv) < 3:
        print("Usage:")
        print("  python usages.py <base_dir> <class_or_method_name>  # Find usage of class/method")
        print("  python usages.py <base_dir> <filename>              # Analyze specific file")
        print("  python usages.py <base_dir> --list                  # List all Java files")
        print("\nExamples:")
        print("  python usages.py src/main/java/com.demo DeviceCredentials")
        print("  python usages.py src/main/java/com.demo EmqxApiClient")
        print("  python usages.py src/main/java/com.demo --list")
        sys.exit(1)

    base_dir = sys.argv[1]
    target = sys.argv[2]

    analyzer = UsageAnalyzer(base_dir)

    if target == "--list":
        # List all Java files in compressed format
        files = analyzer.list_files()
        print(f"📂 Java files in {base_dir} ({len(files)} total):")
        print()

        # Group by directory for better organization
        from collections import defaultdict
        by_dir = defaultdict(list)

        for file_path in sorted(files):
            dir_name = os.path.dirname(file_path)
            if dir_name == "":
                dir_name = "."
            by_dir[dir_name].append(os.path.basename(file_path))

        for dir_name in sorted(by_dir.keys()):
            print(f"📁 {dir_name}/")
            for filename in sorted(by_dir[dir_name]):
                print(f"  • {filename}")
            print()

    elif not target.startswith('--'):
        # Smart detection: check if it's a filename first, then try usage analysis

        # Check if target could be a filename (without .java extension)
        file_found = False
        for filepath in analyzer.java_files:
            if os.path.basename(filepath) == f"{target}.java":
                file_found = True
                break

        if file_found:
            # It's a filename - analyze the file structure
            print(f"🔍 Analyzing file: {target}.java")
            print("=" * 50)

            analysis = analyzer.analyze_file(target)

            if "error" in analysis:
                print(f"❌ Error: {analysis['error']}")
            else:
                print(f"📄 Filename: {analysis['filename']}")
                print(f"📂 Path: {analysis['path']}")

                if analysis['package']:
                    print(f"📦 Package: {analysis['package']}")

                if analysis['imports']:
                    print(f"\n📥 Imports ({len(analysis['imports'])}):")
                    for imp in analysis['imports'][:10]:  # Show first 10
                        print(f"  • {imp}")
                    if len(analysis['imports']) > 10:
                        print(f"  ... and {len(analysis['imports']) - 10} more")

                if analysis['classes']:
                    print(f"\n🏗️ Classes ({len(analysis['classes'])}):")
                    for cls in analysis['classes']:
                        print(f"  • {cls['type']} {cls['name']}")
                        if cls['fields']:
                            print(f"    📊 Fields: {len(cls['fields'])}")
                        if cls['methods']:
                            print(f"    ⚡ Methods: {len(cls['methods'])}")

                if analysis['methods']:
                    print(f"\n⚡ All Methods ({len(analysis['methods'])}):")
                    for method in analysis['methods'][:15]:  # Show first 15
                        print(f"  • {method}")
                    if len(analysis['methods']) > 15:
                        print(f"  ... and {len(analysis['methods']) - 15} more methods")

        else:
            # It's not a filename, so try usage analysis for class/method
            print(f"🔍 Finding usage of: {target}")
            print("=" * 50)

            # Try as class first
            class_usage = analyzer.find_class_usage(target)
            if class_usage:
                print(f"🏗️ Class '{target}' usage found in {len(class_usage)} files:")
                print()
                for file_path, usages in class_usage.items():
                    print(f"📄 {file_path}:")
                    for usage in usages:
                        print(f"  • {usage}")
                    print()

            # Try as method
            method_usage = analyzer.find_method_usage(target)
            if method_usage:
                print(f"⚡ Method '{target}' usage found in {len(method_usage)} files:")
                print()
                for file_path, usages in method_usage.items():
                    print(f"📄 {file_path}:")
                    for usage in usages:
                        print(f"  • {usage}")
                    print()

            if not class_usage and not method_usage:
                print(f"❌ No usage found for '{target}'")
                print("\n💡 Tip: Make sure the class/method name is spelled correctly")
                print("   Or use --list to see all available files")
                print(f"   Or specify the full filename (e.g., {target}.java)")