#!/usr/bin/env python3
"""
Spring Boot Endpoint Extractor
Scans a Spring Boot project and extracts all REST endpoints to a Markdown file.
"""

import os
import re
from pathlib import Path
from datetime import datetime

class EndpointExtractor:
    def __init__(self, root_dir):
        self.root_dir = Path(root_dir)
        self.endpoints = []
        
        # Regex patterns for Spring annotations
        self.patterns = {
            'class_mapping': re.compile(r'@RequestMapping\s*\(\s*["\']([^"\']+)["\']', re.IGNORECASE),
            'class_mapping_value': re.compile(r'@RequestMapping\s*\(\s*value\s*=\s*["\']([^"\']+)["\']', re.IGNORECASE),
            'rest_controller': re.compile(r'@RestController', re.IGNORECASE),
            'controller': re.compile(r'@Controller', re.IGNORECASE),
            'get_mapping': re.compile(r'@GetMapping\s*\(\s*["\']([^"\']+)["\']', re.IGNORECASE),
            'post_mapping': re.compile(r'@PostMapping\s*\(\s*["\']([^"\']+)["\']', re.IGNORECASE),
            'put_mapping': re.compile(r'@PutMapping\s*\(\s*["\']([^"\']+)["\']', re.IGNORECASE),
            'delete_mapping': re.compile(r'@DeleteMapping\s*\(\s*["\']([^"\']+)["\']', re.IGNORECASE),
            'patch_mapping': re.compile(r'@PatchMapping\s*\(\s*["\']([^"\']+)["\']', re.IGNORECASE),
            'request_mapping': re.compile(r'@RequestMapping\s*\([^)]*\)', re.IGNORECASE),
            'method_name': re.compile(r'(public|private|protected)?\s+[\w<>,\s\[\]]+\s+(\w+)\s*\('),
        }
        
    def extract_request_mapping_details(self, annotation_text):
        """Extract method and path from @RequestMapping annotation"""
        method = 'GET'  # default
        path = ''
        
        # Extract method
        method_match = re.search(r'method\s*=\s*RequestMethod\.(\w+)', annotation_text)
        if method_match:
            method = method_match.group(1)
        
        # Extract path/value
        path_match = re.search(r'(?:value|path)\s*=\s*["\']([^"\']+)["\']', annotation_text)
        if path_match:
            path = path_match.group(1)
        else:
            # Try simple form: @RequestMapping("/path")
            simple_match = re.search(r'@RequestMapping\s*\(\s*["\']([^"\']+)["\']', annotation_text)
            if simple_match:
                path = simple_match.group(1)
        
        return method, path
    
    def scan_java_file(self, file_path):
        """Scan a Java file for Spring endpoints"""
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            # Check if this is a controller
            if not (self.patterns['rest_controller'].search(content) or 
                   self.patterns['controller'].search(content)):
                return
            
            # Extract class-level @RequestMapping only if it's on the class declaration line
            class_base_path = ''
            # Find the class declaration first
            class_match = re.search(r'((?:@\w+.*?\n)*)\s*(?:public\s+)?class\s+\w+', content, re.MULTILINE)
            if class_match:
                class_annotations = class_match.group(1)
                class_mapping = self.patterns['class_mapping'].search(class_annotations)
                if not class_mapping:
                    class_mapping = self.patterns['class_mapping_value'].search(class_annotations)
                if class_mapping:
                    class_base_path = class_mapping.group(1)
            
            # Extract class name
            class_match = re.search(r'class\s+(\w+)', content)
            class_name = class_match.group(1) if class_match else 'Unknown'
            
            # Find all method-level mappings
            lines = content.split('\n')
            i = 0
            while i < len(lines):
                line = lines[i].strip()
                
                # Check for mapping annotations
                endpoint_info = None
                
                if '@GetMapping' in line:
                    match = self.patterns['get_mapping'].search(line)
                    if match:
                        endpoint_info = ('GET', match.group(1))
                    else:
                        endpoint_info = ('GET', '')
                
                elif '@PostMapping' in line:
                    match = self.patterns['post_mapping'].search(line)
                    if match:
                        endpoint_info = ('POST', match.group(1))
                    else:
                        endpoint_info = ('POST', '')
                
                elif '@PutMapping' in line:
                    match = self.patterns['put_mapping'].search(line)
                    if match:
                        endpoint_info = ('PUT', match.group(1))
                    else:
                        endpoint_info = ('PUT', '')
                
                elif '@DeleteMapping' in line:
                    match = self.patterns['delete_mapping'].search(line)
                    if match:
                        endpoint_info = ('DELETE', match.group(1))
                    else:
                        endpoint_info = ('DELETE', '')
                
                elif '@PatchMapping' in line:
                    match = self.patterns['patch_mapping'].search(line)
                    if match:
                        endpoint_info = ('PATCH', match.group(1))
                    else:
                        endpoint_info = ('PATCH', '')
                
                elif '@RequestMapping' in line:
                    # Handle multi-line @RequestMapping
                    annotation = line
                    while i < len(lines) - 1 and ')' not in annotation:
                        i += 1
                        annotation += ' ' + lines[i].strip()
                    
                    method, path = self.extract_request_mapping_details(annotation)
                    endpoint_info = (method, path)
                
                if endpoint_info:
                    # Get method name and parameters from next non-empty lines
                    method_name = ''
                    method_params = ''
                    j = i + 1
                    while j < len(lines) and j < i + 5:
                        method_match = self.patterns['method_name'].search(lines[j])
                        if method_match:
                            method_name = method_match.group(2)
                            # Extract parameters for better identification
                            param_match = re.search(r'\(([^)]*)\)', lines[j])
                            if param_match:
                                params = param_match.group(1).strip()
                                if params:
                                    # Simplify parameter display
                                    param_list = [p.strip().split()[-1] for p in params.split(',') if p.strip() and '@' not in p]
                                    method_params = ', '.join(param_list)
                            break
                        j += 1
                    
                    http_method, path = endpoint_info
                    
                    # Only prepend class_base_path if it exists and path doesn't already include it
                    if class_base_path:
                        full_path = class_base_path + path
                    else:
                        full_path = path
                    
                    # Normalize path
                    if not full_path.startswith('/'):
                        full_path = '/' + full_path
                    full_path = re.sub(r'/+', '/', full_path)
                    
                    self.endpoints.append({
                        'method': http_method,
                        'path': full_path,
                        'controller': class_name,
                        'handler': method_name,
                        'file': str(file_path.relative_to(self.root_dir))
                    })
                
                i += 1
                
        except Exception as e:
            print(f"Error reading {file_path}: {e}")
    
    def scan_project(self):
        """Scan entire project for Java files"""
        print(f"Scanning project at: {self.root_dir}")
        java_files = list(self.root_dir.rglob("*.java"))
        print(f"Found {len(java_files)} Java files")
        
        for java_file in java_files:
            self.scan_java_file(java_file)
        
        print(f"Extracted {len(self.endpoints)} endpoints")
    
    def generate_markdown(self, output_file='endpoints.md'):
        """Generate Markdown documentation"""
        # Sort endpoints by path and method
        sorted_endpoints = sorted(self.endpoints, key=lambda x: (x['path'], x['method']))
        
        md_content = f"""# Spring Boot API Endpoints

**Generated on:** {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}  
**Project Directory:** `{self.root_dir}`  
**Total Endpoints:** {len(sorted_endpoints)}

---

## Table of Contents
"""
        
        # Group by controller
        controllers = {}
        for ep in sorted_endpoints:
            controller = ep['controller']
            if controller not in controllers:
                controllers[controller] = []
            controllers[controller].append(ep)
        
        # Add TOC
        for controller in sorted(controllers.keys()):
            md_content += f"- [{controller}](#{controller.lower()})\n"
        
        md_content += "\n---\n\n"
        
        # Add endpoints grouped by controller
        for controller in sorted(controllers.keys()):
            md_content += f"## {controller}\n\n"
            md_content += f"**File:** `{controllers[controller][0]['file']}`\n\n"
            md_content += "| Method | Endpoint | Handler |\n"
            md_content += "|--------|----------|----------|\n"
            
            for ep in controllers[controller]:
                method_badge = f"**{ep['method']}**"
                md_content += f"| {method_badge} | `{ep['path']}` | `{ep['handler']}()` |\n"
            
            md_content += "\n"
        
        # Add complete endpoint list
        md_content += "---\n\n## Complete Endpoint List\n\n"
        md_content += "| Method | Endpoint | Controller | Handler |\n"
        md_content += "|--------|----------|------------|----------|\n"
        
        for ep in sorted_endpoints:
            method_badge = f"**{ep['method']}**"
            md_content += f"| {method_badge} | `{ep['path']}` | {ep['controller']} | `{ep['handler']}()` |\n"
        
        # Write to file
        output_path = self.root_dir / output_file
        with open(output_path, 'w', encoding='utf-8') as f:
            f.write(md_content)
        
        print(f"\n✓ Markdown file generated: {output_path}")
        return output_path


def main():
    # Get current directory or allow custom path
    import sys
    
    if len(sys.argv) > 1:
        project_dir = sys.argv[1]
    else:
        project_dir = os.getcwd()
    
    print("=" * 60)
    print("Spring Boot Endpoint Extractor")
    print("=" * 60)
    
    extractor = EndpointExtractor(project_dir)
    extractor.scan_project()
    
    if extractor.endpoints:
        output_file = extractor.generate_markdown()
        print(f"\n✓ Successfully extracted {len(extractor.endpoints)} endpoints!")
        print(f"✓ Output file: {output_file}")
    else:
        print("\n⚠ No endpoints found. Make sure this is a Spring Boot project.")
    
    print("=" * 60)


if __name__ == "__main__":
    main()