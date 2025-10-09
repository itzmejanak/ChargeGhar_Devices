#!/usr/bin/env python3
"""
Simple Spring Endpoint Extractor
Directly extracts @RequestMapping paths from Java controller files
"""

import os
import re
import json
from pathlib import Path
from typing import List, Dict

class SimpleEndpointExtractor:
    def __init__(self, project_root: str = "."):
        self.project_root = Path(project_root)
        self.controller_path = self.project_root / "src/main/java/com.demo/controller"
        
    def extract_all_endpoints(self) -> Dict:
        """Extract all endpoints and categorize them"""
        print(f"🔍 Scanning: {self.controller_path}")
        
        if not self.controller_path.exists():
            print(f"❌ Controller directory not found!")
            return {}
            
        java_files = list(self.controller_path.glob("*.java"))
        print(f"📁 Found {len(java_files)} Java files")
        
        all_endpoints = []
        
        for java_file in java_files:
            endpoints = self._extract_from_file(java_file)
            all_endpoints.extend(endpoints)
        
        # Categorize endpoints
        categories = self._categorize_endpoints(all_endpoints)
        
        return {
            'all_endpoints': all_endpoints,
            'categories': categories,
            'summary': {
                'total': len(all_endpoints),
                'hardware': len(categories['hardware']),
                'admin': len(categories['admin']),
                'web_ui': len(categories['web_ui']),
                'test': len(categories['test'])
            }
        }
    
    def _extract_from_file(self, file_path: Path) -> List[Dict]:
        """Extract endpoints from a single Java file"""
        endpoints = []
        
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
                
            controller_name = file_path.stem
            print(f"📄 Processing: {controller_name}")
            
            # Find all @RequestMapping annotations
            pattern = r'@RequestMapping\s*\(\s*["\']([^"\']+)["\']'
            matches = re.finditer(pattern, content)
            
            for match in matches:
                path = match.group(1)
                line_num = content[:match.start()].count('\n') + 1
                
                # Find the method name (look for public method after the annotation)
                method_name = self._find_method_name(content, match.end())
                
                endpoint = {
                    'path': path,
                    'controller': controller_name,
                    'method': method_name,
                    'file': str(file_path.relative_to(self.project_root)),
                    'line': line_num
                }
                endpoints.append(endpoint)
                
        except Exception as e:
            print(f"❌ Error processing {file_path}: {e}")
            
        return endpoints
    
    def _find_method_name(self, content: str, start_pos: int) -> str:
        """Find method name after @RequestMapping"""
        # Look for the next public method declaration
        remaining_content = content[start_pos:]
        method_match = re.search(r'public\s+\w+\s+(\w+)\s*\(', remaining_content)
        return method_match.group(1) if method_match else "unknown"
    
    def _categorize_endpoints(self, endpoints: List[Dict]) -> Dict[str, List[Dict]]:
        """Categorize endpoints based on their paths"""
        categories = {
            'hardware': [],    # Must remain unsecured
            'admin': [],       # Need to be secured
            'web_ui': [],      # Web interface
            'test': []         # Test endpoints
        }
        
        for endpoint in endpoints:
            path = endpoint['path']
            
            # Hardware endpoints (CRITICAL - must remain open)
            if any(pattern in path for pattern in [
                '/api/iot/client/con',
                '/api/rentbox/order/return', 
                '/api/rentbox/upload/data',
                '/api/rentbox/config/data',
                '/api/iot/app/version'
            ]):
                categories['hardware'].append(endpoint)
                
            # Web UI endpoints
            elif path.endswith('.html') or path == '/' or path == '/welcome':
                categories['web_ui'].append(endpoint)
                
            # Test endpoints
            elif '/test' in path or '/health' in path:
                categories['test'].append(endpoint)
                
            # Everything else is admin/management
            else:
                categories['admin'].append(endpoint)
        
        return categories
    
    def print_results(self, results: Dict):
        """Print categorized results"""
        print("\n" + "="*70)
        print("🎯 SPRING ENDPOINT ANALYSIS")
        print("="*70)
        
        summary = results['summary']
        print(f"📊 Total endpoints: {summary['total']}")
        print(f"🔧 Hardware endpoints: {summary['hardware']} (KEEP UNSECURED)")
        print(f"🔐 Admin endpoints: {summary['admin']} (NEED SECURITY)")
        print(f"🖥️  Web UI endpoints: {summary['web_ui']}")
        print(f"🧪 Test endpoints: {summary['test']}")
        
        categories = results['categories']
        
        # Hardware endpoints (critical)
        if categories['hardware']:
            print(f"\n🚨 HARDWARE ENDPOINTS (MUST REMAIN OPEN):")
            print("-" * 50)
            for ep in categories['hardware']:
                print(f"  ✅ {ep['path']:35} ({ep['controller']}.{ep['method']})")
        
        # Admin endpoints (need security)
        if categories['admin']:
            print(f"\n🔒 ADMIN ENDPOINTS (NEED API KEY SECURITY):")
            print("-" * 50)
            for ep in categories['admin']:
                print(f"  🔐 {ep['path']:35} ({ep['controller']}.{ep['method']})")
        
        # Web UI endpoints
        if categories['web_ui']:
            print(f"\n🖥️  WEB UI ENDPOINTS:")
            print("-" * 50)
            for ep in categories['web_ui']:
                print(f"  🌐 {ep['path']:35} ({ep['controller']}.{ep['method']})")
        
        # Test endpoints
        if categories['test']:
            print(f"\n🧪 TEST ENDPOINTS:")
            print("-" * 50)
            for ep in categories['test']:
                print(f"  🔬 {ep['path']:35} ({ep['controller']}.{ep['method']})")
        
        print("\n" + "="*70)
    
    def save_results(self, results: Dict, filename: str = "endpoint_analysis.json"):
        """Save results to JSON file"""
        with open(filename, 'w', encoding='utf-8') as f:
            json.dump(results, f, indent=2, ensure_ascii=False)
        print(f"💾 Results saved to: {filename}")
    
    def generate_security_config(self, results: Dict) -> str:
        """Generate security configuration recommendations"""
        categories = results['categories']
        
        config = []
        config.append("# Spring Security Configuration Recommendations")
        config.append("# Based on endpoint analysis")
        config.append("")
        
        # Hardware endpoints to exclude from security
        if categories['hardware']:
            config.append("## Hardware Endpoints - EXCLUDE from Security Filter")
            config.append("# These endpoints MUST remain completely open for device communication")
            for ep in categories['hardware']:
                config.append(f"# EXCLUDE: {ep['path']} - {ep['controller']}.{ep['method']}")
            config.append("")
        
        # Admin endpoints to secure
        if categories['admin']:
            config.append("## Admin Endpoints - REQUIRE API Key Authentication")
            config.append("# These endpoints need API key security")
            for ep in categories['admin']:
                config.append(f"# SECURE: {ep['path']} - {ep['controller']}.{ep['method']}")
            config.append("")
        
        # Web UI endpoints
        if categories['web_ui']:
            config.append("## Web UI Endpoints - REQUIRE Admin Login")
            config.append("# These endpoints need admin session authentication")
            for ep in categories['web_ui']:
                config.append(f"# ADMIN_LOGIN: {ep['path']} - {ep['controller']}.{ep['method']}")
            config.append("")
        
        return "\n".join(config)

def main():
    """Main function"""
    print("🚀 Simple Spring Endpoint Extractor")
    print("-" * 50)
    
    extractor = SimpleEndpointExtractor()
    results = extractor.extract_all_endpoints()
    
    if not results['all_endpoints']:
        print("❌ No endpoints found!")
        return
    
    # Print results
    extractor.print_results(results)
    
    # Save to JSON
    extractor.save_results(results)
    
    # Generate security config
    security_config = extractor.generate_security_config(results)
    with open("security_config_recommendations.txt", 'w') as f:
        f.write(security_config)
    
    print(f"🔒 Security recommendations saved to: security_config_recommendations.txt")
    
    # Final summary
    print(f"\n✅ NEXT STEPS:")
    print(f"1. Review hardware endpoints - these MUST stay unsecured")
    print(f"2. Plan API key security for {results['summary']['admin']} admin endpoints")
    print(f"3. Plan session auth for {results['summary']['web_ui']} web UI endpoints")

if __name__ == "__main__":
    main()