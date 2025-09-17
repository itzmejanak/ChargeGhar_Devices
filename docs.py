#!/usr/bin/env python3
"""
Documentation Generator for Java Codebase

This script analyzes Java source files in specified directories and generates
documentation listing classes, methods, and their usages throughout the codebase.

Usage:
    python docs.py list                                    # List all directories
    python docs.py --dir <dirname>                          # Generate docs for specific directory
    python docs.py --dir <dirname> --md                     # Generate markdown file (<dirname>_docs.md)
    python docs.py --dir <dirname> --base <path> --md       # Custom base directory

Requirements:
    pip install javalang

The script parses Java files using javalang to extract:
- Class declarations and interface declarations
- Method signatures with return types and parameters
- Usage analysis showing where classes/methods are referenced across the codebase
"""

import os
import sys
import argparse
import re
from collections import defaultdict

try:
    import javalang
    from usages import find_usages
except ImportError as e:
    if 'javalang' in str(e):
        print("Error: javalang library is required. Install with: pip install javalang")
    else:
        print("Error: usages module not found. Make sure usages.py is in the same directory.")
    sys.exit(1)

def get_java_files(dir_path):
    """Recursively find all .java files in the given directory."""
    java_files = []
    for root, dirs, files in os.walk(dir_path):
        for file in files:
            if file.endswith('.java'):
                java_files.append(os.path.join(root, file))
    return java_files

# find_usages function is now imported from usages.py module

def group_getter_setters(methods):
    """
    Group getter and setter methods by property for more concise output.

    Returns:
        tuple: (grouped_properties, remaining_methods)
            grouped_properties: dict of property -> {'getter': method, 'setter': method}
            remaining_methods: list of non-getter/setter methods
    """
    properties = {}
    remaining = []

    for method in methods:
        method_match = re.match(r'(String|boolean|int|long|double|float|byte|char|short|void|Date|Object|List|Map)\s+(get|set|is)(\w+)\s*\(', method)
        if method_match:
            return_type, prefix, prop_name = method_match.groups()
            prop_key = prop_name.lower()

            if prop_key not in properties:
                properties[prop_key] = {}

            if prefix in ['get', 'is']:
                properties[prop_key]['getter'] = method
            elif prefix == 'set':
                properties[prop_key]['setter'] = method
        else:
            remaining.append(method)

    return properties, remaining

def format_property_group(prop_name, prop_data):
    """Format a grouped getter/setter property for output."""
    lines = []
    if 'getter' in prop_data:
        getter_match = re.match(r'(.+)\s+get(\w+)\s*\((.*)\)', prop_data['getter'])
        if getter_match:
            return_type, method_name, params = getter_match.groups()
            lines.append(f"- **get{method_name}()**: {return_type}")

    if 'setter' in prop_data:
        setter_match = re.match(r'(.+)\s+set(\w+)\s*\((.*)\)', prop_data['setter'])
        if setter_match:
            return_type, method_name, params = setter_match.groups()
            param_match = re.match(r'(\w+)\s+(\w+)', params.strip())
            if param_match:
                param_type, param_name = param_match.groups()
                lines.append(f"- **set{method_name}({param_type} {param_name})**: {return_type}")

    return '\n'.join(lines)

def parse_java_file(filepath):
    """
    Parse a Java file and extract classes and methods.

    Returns:
        tuple: (classes_list, methods_list)
            classes_list: list of class names
            methods_list: list of method signatures as strings
    """
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        # Fallback to latin-1 if utf-8 fails
        with open(filepath, 'r', encoding='latin-1') as f:
            content = f.read()

    classes = []
    methods = []

    try:
        tree = javalang.parse.parse(content)
        for path, node in tree:
            if isinstance(node, javalang.tree.ClassDeclaration):
                classes.append(node.name)
                for method in node.methods:
                    # Build method signature
                    return_type = method.return_type.name if method.return_type else 'void'
                    method_name = method.name
                    params = []
                    for param in method.parameters:
                        param_type = param.type.name if hasattr(param.type, 'name') else str(param.type)
                        param_name = param.name
                        params.append(f"{param_type} {param_name}")
                    params_str = ', '.join(params)
                    signature = f"{return_type} {method_name}({params_str})"
                    methods.append(signature)
            elif isinstance(node, javalang.tree.InterfaceDeclaration):
                classes.append(f"interface {node.name}")
                # Interfaces have methods too, but they are abstract
                for method in node.methods:
                    return_type = method.return_type.name if method.return_type else 'void'
                    method_name = method.name
                    params = []
                    for param in method.parameters:
                        param_type = param.type.name if hasattr(param.type, 'name') else str(param.type)
                        param_name = param.name
                        params.append(f"{param_type} {param_name}")
                    params_str = ', '.join(params)
                    signature = f"{return_type} {method_name}({params_str})"
                    methods.append(signature)
    except javalang.parser.JavaSyntaxError as e:
        print(f"Warning: Could not parse {filepath} due to syntax error: {e}")
    except Exception as e:
        print(f"Warning: Error parsing {filepath}: {e}")

    return classes, methods

def list_dirs(base_dir):
    """List all subdirectories in the base directory."""
    try:
        subdirs = [d for d in os.listdir(base_dir) if os.path.isdir(os.path.join(base_dir, d))]
        return sorted(subdirs)
    except FileNotFoundError:
        print(f"Error: Base directory {base_dir} not found.")
        return []

def generate_docs_for_dir(dir_path, dir_name, base_dir, output_md=False, mode='normal'):
    """Generate documentation for a specific directory."""
    if not os.path.exists(dir_path):
        print(f"Error: Directory {dir_path} does not exist.")
        return

    java_files = get_java_files(dir_path)

    if not java_files:
        print(f"No Java files found in {dir_path}")
        return

    # Collect all classes and methods for usage analysis
    all_classes = []
    all_methods = []
    file_data = {}

    for java_file in sorted(java_files):
        classes, methods = parse_java_file(java_file)
        rel_path = os.path.relpath(java_file, dir_path)
        file_data[rel_path] = {'classes': classes, 'methods': methods}
        all_classes.extend(classes)
        all_methods.extend(methods)

    # Find usages throughout the codebase
    detailed_usages = find_usages(base_dir, all_classes, all_methods)

    # Convert to the expected format for backward compatibility
    usages = defaultdict(list)
    for name, info in detailed_usages.items():
        if 'usages' in info and info['usages']:
            usages[name] = list(info['usages'].keys())

    if output_md:
        # Generate markdown file
        md_filename = f"{dir_name}_docs.md"
        md_path = os.path.join(dir_path, md_filename)

        with open(md_path, 'w', encoding='utf-8') as f:
            f.write(f"# Documentation for Directory: {dir_name}\n\n")
            f.write(f"**Path:** {dir_path}\n\n")
            f.write(f"**Total Java files:** {len(java_files)}\n\n")
            f.write(f"**Output mode:** {mode}\n\n")

            # Calculate summary statistics
            total_classes = sum(len(file_data[rel_path]['classes']) for rel_path in file_data)
            total_methods = sum(len(file_data[rel_path]['methods']) for rel_path in file_data)

            f.write("## Summary\n\n")
            f.write(f"- **Files:** {len(java_files)}\n")
            f.write(f"- **Classes:** {total_classes}\n")
            f.write(f"- **Methods:** {total_methods}\n\n")

            if mode != 'brief':
                f.write("## Table of Contents\n\n")
                for rel_path in sorted(file_data.keys()):
                    f.write(f"- [{rel_path}](#{rel_path.replace('/', '-').replace('.', '-')})\n")
                f.write("\n")

            for rel_path in sorted(file_data.keys()):
                f.write(f"## {rel_path}\n\n")
                classes = file_data[rel_path]['classes']
                methods = file_data[rel_path]['methods']

                if classes:
                    f.write("### Classes\n\n")
                    for cls in classes:
                        f.write(f"#### {cls}\n\n")
                        # Show usage
                        cls_name = cls.replace('interface ', '')
                        if cls_name in detailed_usages and detailed_usages[cls_name]['usages']:
                            if mode == 'detailed':
                                # Detailed mode: show usage types
                                f.write("**Used in:**\n")
                                for usage_file, usage_details in detailed_usages[cls_name]['usages'].items():
                                    f.write(f"- **{usage_file}:**\n")
                                    for detail in usage_details:
                                        f.write(f"  - {detail}\n")
                                f.write("\n")
                            else:
                                # Brief/normal mode: just file list
                                f.write("**Used in:**\n")
                                for usage_file in detailed_usages[cls_name]['usages'].keys():
                                    f.write(f"- {usage_file}\n")
                                f.write("\n")
                        elif mode == 'detailed':
                            f.write("*No usage found in codebase*\n\n")

                if methods:
                    # Group getter/setter methods for optimization
                    properties, remaining_methods = group_getter_setters(methods)

                    if properties and mode != 'brief':
                        f.write("### Properties (Getters/Setters)\n\n")
                        for prop_name, prop_data in sorted(properties.items()):
                            f.write(f"**{prop_name}:**\n")
                            f.write(format_property_group(prop_name, prop_data) + "\n\n")

                    if remaining_methods:
                        f.write("### Methods\n\n")
                        for method in remaining_methods:
                            if mode == 'brief':
                                # Brief mode: just method signature
                                f.write(f"- `{method}`\n")
                            else:
                                f.write(f"#### `{method}`\n\n")
                                # Extract method name for usage lookup
                                method_match = re.match(r'.*\s+(\w+)\s*\(', method)
                                if method_match:
                                    method_name = method_match.group(1)
                                    if method_name in detailed_usages and detailed_usages[method_name]['usages']:
                                        if mode == 'detailed':
                                            # Detailed mode: show usage types
                                            f.write("**Called in:**\n")
                                            for usage_file, usage_details in detailed_usages[method_name]['usages'].items():
                                                f.write(f"- **{usage_file}:**\n")
                                                for detail in usage_details:
                                                    f.write(f"  - {detail}\n")
                                            f.write("\n")
                                        else:
                                            # Brief/normal mode: just file list
                                            f.write("**Called in:**\n")
                                            for usage_file in detailed_usages[method_name]['usages'].keys():
                                                f.write(f"- {usage_file}\n")
                                            f.write("\n")
                                    elif mode == 'detailed':
                                        f.write("*No calls found in codebase*\n\n")

                if not classes and not methods:
                    f.write("*No classes or methods found*\n\n")

                if mode != 'brief':
                    f.write("---\n\n")

        print(f"Markdown documentation generated: {md_path} (Mode: {mode})")

    else:
        # Console output
        print(f"\n{'='*60}")
        print(f"DOCUMENTATION FOR DIRECTORY: {dir_name}")
        print(f"{'='*60}")
        print(f"Path: {dir_path}")
        print(f"Total Java files: {len(java_files)}")
        print()

        for rel_path in sorted(file_data.keys()):
            print(f"File: {rel_path}")
            classes = file_data[rel_path]['classes']
            methods = file_data[rel_path]['methods']

            if classes:
                print("  Classes:")
                for cls in classes:
                    print(f"    - {cls}")
                    cls_name = cls.replace('interface ', '')
                    if cls_name in usages and usages[cls_name]:
                        print(f"      Used in: {', '.join(usages[cls_name])}")
            else:
                print("  Classes: None")

            if methods:
                print("  Methods:")
                for method in methods:
                    print(f"    - {method}")
                    method_match = re.match(r'.*\s+(\w+)\s*\(', method)
                    if method_match:
                        method_name = method_match.group(1)
                        if method_name in usages and usages[method_name]:
                            print(f"      Called in: {', '.join(usages[method_name])}")
            else:
                print("  Methods: None")
            print()

def main():
    parser = argparse.ArgumentParser(
        description='Generate documentation for Java classes and methods in directories.',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__
    )
    parser.add_argument('--dir', help='Specific directory to analyze (relative to base)')
    parser.add_argument('--base', default='src/main/java/com.demo',
                       help='Base directory for analysis (default: src/main/java/com.demo)')
    parser.add_argument('--md', action='store_true',
                       help='Generate markdown file output (<dirname>_docs.md)')
    parser.add_argument('--mode', choices=['brief', 'normal', 'detailed'],
                       default='normal', help='Output detail level (default: normal)')
    parser.add_argument('command', nargs='?', choices=['list'], help='Command: list to list directories')

    args = parser.parse_args()

    base_dir = args.base

    if args.command == 'list':
        print(f"Available directories in {base_dir}:")
        dirs = list_dirs(base_dir)
        if dirs:
            for d in dirs:
                print(f"  - {d}")
        else:
            print("  No directories found.")
    elif args.dir:
        dir_path = os.path.join(base_dir, args.dir)
        generate_docs_for_dir(dir_path, args.dir, base_dir, args.md, args.mode)
    else:
        print("Usage:")
        print("  python docs.py list                                    # List all directories")
        print("  python docs.py --dir <dirname>                          # Generate docs for specific directory")
        print("  python docs.py --dir <dirname> --md                     # Generate markdown file")
        print("  python docs.py --dir <dirname> --base <path> --md      # Custom base directory")
        print("  python docs.py --dir <dirname> --md --mode brief       # Brief mode (optimized)")
        print("\nModes:")
        print("  brief   - Compact output, groups getters/setters, minimal usage info")
        print("  normal  - Standard output with grouped properties (default)")
        print("  detailed- Verbose output with all usage information")
        print("\nExamples:")
        print("  python docs.py --dir common --md --mode brief         # Compact docs")
        print("  python docs.py --dir mqtt --md --mode detailed        # Full details")
        print("  python docs.py --dir tools --base src/main/java --md  # Broader scan")
        print("\nRun 'python docs.py list' to see available directories.")

if __name__ == '__main__':
    main()