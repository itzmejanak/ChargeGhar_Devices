#!/usr/bin/env python3
"""
Universal Docker Deployment Manager
Beautiful menu-driven management tool for any Docker Compose project
"""

import os
import sys
import subprocess
import time
import glob
from datetime import datetime
from pathlib import Path

class Colors:
    HEADER = '\033[95m'
    BLUE = '\033[94m'
    CYAN = '\033[96m'
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'

class DockerManager:
    def __init__(self):
        self.project_dir = os.getcwd()
        self.project_name = os.path.basename(self.project_dir)
        self.compose_file = self.detect_compose_file()
        self.services = self.detect_services()
        self.env_vars = self.load_env_vars()
        
    def detect_compose_file(self):
        """Auto-detect docker-compose file"""
        compose_files = [
            'docker-compose.prod.yml',
            'docker-compose.production.yml',
            'docker-compose.yml',
            'docker-compose.yaml',
            'compose.yml',
            'compose.yaml'
        ]
        
        for cf in compose_files:
            if os.path.exists(os.path.join(self.project_dir, cf)):
                print(f"{Colors.GREEN}✅ Found compose file: {cf}{Colors.ENDC}")
                return cf
        
        # If no file found, list all yml files
        yml_files = glob.glob('*.yml') + glob.glob('*.yaml')
        if yml_files:
            print(f"{Colors.YELLOW}📋 Available compose files:{Colors.ENDC}")
            for i, f in enumerate(yml_files, 1):
                print(f"  {i}. {f}")
            try:
                choice = int(input(f"{Colors.YELLOW}Select file (1-{len(yml_files)}): {Colors.ENDC}"))
                if 1 <= choice <= len(yml_files):
                    return yml_files[choice - 1]
            except:
                pass
        
        print(f"{Colors.RED}❌ No docker-compose file found{Colors.ENDC}")
        return "docker-compose.yml"
    
    def detect_services(self):
        """Detect services from docker-compose file"""
        try:
            result = subprocess.run(
                f"docker-compose -f {self.compose_file} config --services",
                shell=True,
                capture_output=True,
                text=True,
                cwd=self.project_dir
            )
            if result.returncode == 0:
                services = [s.strip() for s in result.stdout.strip().split('\n') if s.strip()]
                return services
        except:
            pass
        return []
    
    def load_env_vars(self):
        """Load environment variables from .env file"""
        env_vars = {}
        env_file = os.path.join(self.project_dir, '.env')
        if os.path.exists(env_file):
            try:
                with open(env_file, 'r') as f:
                    for line in f:
                        line = line.strip()
                        if line and not line.startswith('#') and '=' in line:
                            key, value = line.split('=', 1)
                            env_vars[key.strip()] = value.strip()
            except:
                pass
        return env_vars
    
    def get_exposed_ports(self):
        """Get exposed ports from running containers"""
        try:
            result = subprocess.run(
                f"docker-compose -f {self.compose_file} ps --format json",
                shell=True,
                capture_output=True,
                text=True,
                cwd=self.project_dir
            )
            if result.returncode == 0:
                import json
                containers = []
                for line in result.stdout.strip().split('\n'):
                    if line.strip():
                        try:
                            containers.append(json.loads(line))
                        except:
                            pass
                return containers
        except:
            pass
        return []
        
    def print_header(self):
        print(f"\n{Colors.CYAN}{'='*70}{Colors.ENDC}")
        print(f"{Colors.BOLD}{Colors.HEADER}🐳 Universal Docker Deployment Manager{Colors.ENDC}")
        print(f"{Colors.CYAN}{'='*70}{Colors.ENDC}")
        print(f"{Colors.YELLOW}Project: {self.project_name}{Colors.ENDC}")
        print(f"{Colors.YELLOW}Directory: {self.project_dir}{Colors.ENDC}")
        print(f"{Colors.YELLOW}Compose File: {self.compose_file}{Colors.ENDC}")
        print(f"{Colors.YELLOW}Services: {len(self.services)}{Colors.ENDC}")
        print(f"{Colors.YELLOW}Time: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}{Colors.ENDC}")
        print(f"{Colors.CYAN}{'='*70}{Colors.ENDC}\n")

    def print_menu(self):
        menu_items = [
            ("1", "🔄 Restart Services", "Restart all containers"),
            ("2", "⏹️  Stop Services", "Stop all containers"),
            ("3", "▶️  Start Services", "Start all containers"),
            ("4", "🗑️  Delete All", "Stop and remove containers, volumes, images"),
            ("5", "📊 Service Status", "Show current status of all services"),
            ("6", "📋 View Logs", "View logs for services"),
            ("7", "🏗️  Rebuild Services", "Rebuild and restart services"),
            ("8", "🏥 Health Check", "Check service health and connectivity"),
            ("9", "💾 Database Backup", "Create database backup (PostgreSQL/MySQL)"),
            ("10", "🧹 Clean Docker", "Clean unused Docker resources"),
            ("11", "🔓 Clear Port Conflicts", "Fix port conflicts"),
            ("12", "🔍 Inspect Services", "Show detected services and ports"),
            ("13", "⚡ Execute Command", "Run custom command in container"),
            ("14", "🔧 Shell Access", "Get shell access to container"),
            ("0", "❌ Exit", "Exit the manager")
        ]
        
        print(f"{Colors.BOLD}{Colors.BLUE}Available Actions:{Colors.ENDC}\n")
        for num, title, desc in menu_items:
            print(f"{Colors.GREEN}{num:>2}.{Colors.ENDC} {Colors.BOLD}{title:<22}{Colors.ENDC} - {desc}")
        print()

    def run_command(self, command, description="", show_output=True):
        """Run a shell command with nice formatting"""
        if description:
            print(f"{Colors.BLUE}[RUNNING]{Colors.ENDC} {description}...")
        
        try:
            if show_output:
                result = subprocess.run(command, shell=True, check=True, cwd=self.project_dir)
                return result.returncode == 0
            else:
                result = subprocess.run(command, shell=True, check=True, capture_output=True, text=True, cwd=self.project_dir)
                return result.stdout.strip()
        except subprocess.CalledProcessError as e:
            if show_output:
                print(f"{Colors.RED}[ERROR]{Colors.ENDC} Command failed: {e}")
            return False

    def clear_port_conflicts(self):
        """Clear any port conflicts"""
        print(f"{Colors.BLUE}🔍 Checking for port conflicts...{Colors.ENDC}")
        
        # Get all ports from environment variables
        ports = []
        for key, value in self.env_vars.items():
            if 'PORT' in key.upper():
                try:
                    port = int(value)
                    if 1 <= port <= 65535:
                        ports.append(port)
                except:
                    pass
        
        if not ports:
            # Common ports to check
            ports = [8000, 8010, 8080, 3000, 5000, 5432, 3306, 6379, 5672]
        
        print(f"{Colors.YELLOW}Checking ports: {', '.join(map(str, ports))}{Colors.ENDC}")
        
        for port in ports:
            conflicting = self.run_command(f'docker ps --filter "publish={port}" --format "{{{{.Names}}}}"', show_output=False)
            if conflicting:
                print(f"{Colors.YELLOW}Found containers using port {port}:{Colors.ENDC}")
                for container in conflicting.split('\n'):
                    if container.strip():
                        print(f"  Stopping: {container}")
                        self.run_command(f"docker stop {container}", show_output=False)
                        self.run_command(f"docker rm {container}", show_output=False)
        
        print(f"{Colors.GREEN}✅ Port conflicts cleared{Colors.ENDC}")

    def restart_services(self):
        """Restart all services"""
        print(f"\n{Colors.YELLOW}🔄 Restarting Services...{Colors.ENDC}")
        
        self.clear_port_conflicts()
        
        self.run_command(f"docker-compose -f {self.compose_file} down", "Stopping containers")
        time.sleep(2)
        success = self.run_command(f"docker-compose -f {self.compose_file} up -d", "Starting containers")
        
        if success:
            print(f"{Colors.GREEN}✅ Services restarted successfully!{Colors.ENDC}")
            time.sleep(3)
            self.show_status()
        else:
            print(f"{Colors.RED}❌ Failed to restart services{Colors.ENDC}")

    def stop_services(self):
        """Stop all services"""
        print(f"\n{Colors.YELLOW}⏹️ Stopping Services...{Colors.ENDC}")
        success = self.run_command(f"docker-compose -f {self.compose_file} down --remove-orphans", "Stopping containers")
        if success:
            print(f"{Colors.GREEN}✅ Services stopped successfully!{Colors.ENDC}")
        else:
            print(f"{Colors.RED}❌ Failed to stop services{Colors.ENDC}")

    def start_services(self):
        """Start all services"""
        print(f"\n{Colors.YELLOW}▶️ Starting Services...{Colors.ENDC}")
        success = self.run_command(f"docker-compose -f {self.compose_file} up -d", "Starting containers")
        if success:
            print(f"{Colors.GREEN}✅ Services started successfully!{Colors.ENDC}")
            time.sleep(3)
            self.show_status()
        else:
            print(f"{Colors.RED}❌ Failed to start services{Colors.ENDC}")

    def delete_all(self):
        """Delete all containers, volumes, and images"""
        print(f"\n{Colors.RED}🗑️ WARNING: This will delete ALL project data!{Colors.ENDC}")
        confirm = input(f"{Colors.YELLOW}Type 'DELETE' to confirm: {Colors.ENDC}")
        
        if confirm == "DELETE":
            print(f"{Colors.RED}Deleting all resources...{Colors.ENDC}")
            commands = [
                f"docker-compose -f {self.compose_file} down --remove-orphans --volumes --rmi all",
                "docker system prune -af",
                "docker volume prune -f"
            ]
            
            for cmd in commands:
                self.run_command(cmd)
            
            print(f"{Colors.GREEN}✅ All resources deleted!{Colors.ENDC}")
        else:
            print(f"{Colors.YELLOW}❌ Deletion cancelled{Colors.ENDC}")

    def show_status(self):
        """Show service status"""
        print(f"\n{Colors.BLUE}📊 Service Status:{Colors.ENDC}")
        self.run_command(f"docker-compose -f {self.compose_file} ps")

    def view_logs(self):
        """View logs with service selection"""
        print(f"\n{Colors.BLUE}📋 Available Services:{Colors.ENDC}")
        services = ["all"] + self.services
        
        for i, service in enumerate(services, 1):
            print(f"{Colors.GREEN}{i}.{Colors.ENDC} {service}")
        
        try:
            choice = int(input(f"\n{Colors.YELLOW}Select service (1-{len(services)}): {Colors.ENDC}"))
            if 1 <= choice <= len(services):
                service = services[choice - 1]
                if service == "all":
                    cmd = f"docker-compose -f {self.compose_file} logs --tail=50 -f"
                else:
                    cmd = f"docker-compose -f {self.compose_file} logs --tail=50 -f {service}"
                
                print(f"{Colors.BLUE}Showing logs for {service} (Press Ctrl+C to exit)...{Colors.ENDC}")
                self.run_command(cmd)
            else:
                print(f"{Colors.RED}Invalid choice{Colors.ENDC}")
        except (ValueError, KeyboardInterrupt):
            print(f"\n{Colors.YELLOW}Returning to menu...{Colors.ENDC}")

    def rebuild_services(self):
        """Rebuild and restart services"""
        print(f"\n{Colors.YELLOW}🏗️  Rebuilding Services...{Colors.ENDC}")
        confirm = input(f"{Colors.YELLOW}This will rebuild all images. Continue? (y/N): {Colors.ENDC}")
        
        if confirm.lower() == 'y':
            self.clear_port_conflicts()
            success = self.run_command(f"docker-compose -f {self.compose_file} up -d --build", "Building and starting")
            if success:
                print(f"{Colors.GREEN}✅ Services rebuilt successfully!{Colors.ENDC}")
                time.sleep(3)
                self.show_status()
            else:
                print(f"{Colors.RED}❌ Rebuild failed{Colors.ENDC}")
        else:
            print(f"{Colors.YELLOW}❌ Rebuild cancelled{Colors.ENDC}")

    def health_check(self):
        """Check service health"""
        print(f"\n{Colors.BLUE}🏥 Checking Service Health...{Colors.ENDC}")
        
        # Check running services
        status = self.run_command(f"docker-compose -f {self.compose_file} ps --services --filter status=running", show_output=False)
        if status:
            running_services = [s for s in status.split('\n') if s.strip()]
            print(f"{Colors.GREEN}Running Services: {len(running_services)}/{len(self.services)}{Colors.ENDC}")
            for service in running_services:
                print(f"  ✅ {service}")
            
            stopped = set(self.services) - set(running_services)
            if stopped:
                print(f"\n{Colors.RED}Stopped Services:{Colors.ENDC}")
                for service in stopped:
                    print(f"  ❌ {service}")
        
        # Check common health endpoints
        health_endpoints = [
            '/health', '/api/health', '/healthz', 
            '/api/app/health', '/_health', '/status'
        ]
        
        # Try to find ports
        for key, value in self.env_vars.items():
            if 'PORT' in key.upper() and key.upper() not in ['DB_PORT', 'REDIS_PORT', 'RABBITMQ_PORT']:
                try:
                    port = int(value)
                    print(f"\n{Colors.BLUE}Testing endpoints on port {port}:{Colors.ENDC}")
                    for endpoint in health_endpoints:
                        url = f"http://localhost:{port}{endpoint}"
                        result = self.run_command(f"curl -s -f -m 2 {url}", show_output=False)
                        if result:
                            print(f"  {Colors.GREEN}✅ {url}{Colors.ENDC}")
                            print(f"     {result[:100]}...")
                            break
                except:
                    pass

    def database_backup(self):
        """Create database backup"""
        print(f"\n{Colors.BLUE}💾 Creating Database Backup...{Colors.ENDC}")
        
        # Find database service
        db_services = [s for s in self.services if any(db in s.lower() for db in ['db', 'postgres', 'mysql', 'mariadb'])]
        
        if not db_services:
            print(f"{Colors.RED}❌ No database service found{Colors.ENDC}")
            return
        
        print(f"{Colors.YELLOW}Found database services: {', '.join(db_services)}{Colors.ENDC}")
        db_service = db_services[0]
        
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        backup_dir = "backups"
        os.makedirs(backup_dir, exist_ok=True)
        
        # Determine database type
        if 'postgres' in db_service.lower():
            db_user = self.env_vars.get('POSTGRES_USER', self.env_vars.get('DB_USER', 'postgres'))
            db_name = self.env_vars.get('POSTGRES_DB', self.env_vars.get('DB_NAME', 'postgres'))
            backup_file = f"{backup_dir}/{self.project_name}_postgres_{timestamp}.sql"
            cmd = f"docker-compose -f {self.compose_file} exec -T {db_service} pg_dump -U {db_user} {db_name} > {backup_file}"
        elif 'mysql' in db_service.lower() or 'mariadb' in db_service.lower():
            db_user = self.env_vars.get('MYSQL_USER', self.env_vars.get('DB_USER', 'root'))
            db_pass = self.env_vars.get('MYSQL_PASSWORD', self.env_vars.get('DB_PASSWORD', ''))
            db_name = self.env_vars.get('MYSQL_DATABASE', self.env_vars.get('DB_NAME', ''))
            backup_file = f"{backup_dir}/{self.project_name}_mysql_{timestamp}.sql"
            cmd = f"docker-compose -f {self.compose_file} exec -T {db_service} mysqldump -u{db_user} -p{db_pass} {db_name} > {backup_file}"
        else:
            print(f"{Colors.RED}❌ Unknown database type{Colors.ENDC}")
            return
        
        success = self.run_command(cmd, f"Creating backup: {backup_file}")
        if success:
            print(f"{Colors.GREEN}✅ Backup created: {backup_file}{Colors.ENDC}")
        else:
            print(f"{Colors.RED}❌ Backup failed{Colors.ENDC}")

    def clean_docker(self):
        """Clean unused Docker resources"""
        print(f"\n{Colors.BLUE}🧹 Cleaning Docker Resources...{Colors.ENDC}")
        commands = [
            ("docker system prune -f", "Cleaning system"),
            ("docker volume prune -f", "Cleaning volumes"),
            ("docker image prune -f", "Cleaning images")
        ]
        
        for cmd, desc in commands:
            self.run_command(cmd, desc)
        
        print(f"{Colors.GREEN}✅ Docker cleanup completed!{Colors.ENDC}")

    def inspect_services(self):
        """Show detected services and configuration"""
        print(f"\n{Colors.BLUE}🔍 Project Inspection:{Colors.ENDC}")
        print(f"\n{Colors.BOLD}Detected Services ({len(self.services)}):{Colors.ENDC}")
        for service in self.services:
            print(f"  • {service}")
        
        print(f"\n{Colors.BOLD}Environment Variables:{Colors.ENDC}")
        for key, value in sorted(self.env_vars.items()):
            # Mask sensitive values
            if any(s in key.upper() for s in ['PASSWORD', 'SECRET', 'KEY', 'TOKEN']):
                display_value = '*' * 8
            else:
                display_value = value
            print(f"  {key} = {display_value}")
        
        print(f"\n{Colors.BOLD}Running Containers:{Colors.ENDC}")
        self.run_command(f"docker-compose -f {self.compose_file} ps")

    def execute_command(self):
        """Execute custom command in container"""
        print(f"\n{Colors.BLUE}⚡ Execute Command in Container:{Colors.ENDC}")
        
        if not self.services:
            print(f"{Colors.RED}❌ No services available{Colors.ENDC}")
            return
        
        for i, service in enumerate(self.services, 1):
            print(f"{Colors.GREEN}{i}.{Colors.ENDC} {service}")
        
        try:
            choice = int(input(f"\n{Colors.YELLOW}Select service (1-{len(self.services)}): {Colors.ENDC}"))
            if 1 <= choice <= len(self.services):
                service = self.services[choice - 1]
                command = input(f"{Colors.YELLOW}Enter command: {Colors.ENDC}")
                
                if command.strip():
                    self.run_command(f"docker-compose -f {self.compose_file} exec {service} {command}")
        except (ValueError, KeyboardInterrupt):
            print(f"\n{Colors.YELLOW}Operation cancelled{Colors.ENDC}")

    def shell_access(self):
        """Get shell access to container"""
        print(f"\n{Colors.BLUE}🔧 Shell Access:{Colors.ENDC}")
        
        if not self.services:
            print(f"{Colors.RED}❌ No services available{Colors.ENDC}")
            return
        
        for i, service in enumerate(self.services, 1):
            print(f"{Colors.GREEN}{i}.{Colors.ENDC} {service}")
        
        try:
            choice = int(input(f"\n{Colors.YELLOW}Select service (1-{len(self.services)}): {Colors.ENDC}"))
            if 1 <= choice <= len(self.services):
                service = self.services[choice - 1]
                
                # Try different shells
                shells = ['/bin/bash', '/bin/sh', 'sh', 'bash']
                for shell in shells:
                    result = self.run_command(f"docker-compose -f {self.compose_file} exec {service} {shell}", show_output=False)
                    if result is not False:
                        self.run_command(f"docker-compose -f {self.compose_file} exec {service} {shell}")
                        return
                
                print(f"{Colors.RED}❌ Could not access shell{Colors.ENDC}")
        except (ValueError, KeyboardInterrupt):
            print(f"\n{Colors.YELLOW}Operation cancelled{Colors.ENDC}")

    def run(self):
        """Main menu loop"""
        while True:
            try:
                os.system('clear' if os.name == 'posix' else 'cls')
                self.print_header()
                self.print_menu()
                
                choice = input(f"{Colors.BOLD}Enter your choice (0-14): {Colors.ENDC}").strip()
                
                if choice == "0":
                    print(f"\n{Colors.GREEN}👋 Goodbye!{Colors.ENDC}")
                    break
                elif choice == "1":
                    self.restart_services()
                elif choice == "2":
                    self.stop_services()
                elif choice == "3":
                    self.start_services()
                elif choice == "4":
                    self.delete_all()
                elif choice == "5":
                    self.show_status()
                elif choice == "6":
                    self.view_logs()
                elif choice == "7":
                    self.rebuild_services()
                elif choice == "8":
                    self.health_check()
                elif choice == "9":
                    self.database_backup()
                elif choice == "10":
                    self.clean_docker()
                elif choice == "11":
                    self.clear_port_conflicts()
                elif choice == "12":
                    self.inspect_services()
                elif choice == "13":
                    self.execute_command()
                elif choice == "14":
                    self.shell_access()
                else:
                    print(f"{Colors.RED}❌ Invalid choice. Please try again.{Colors.ENDC}")
                
                if choice != "0":
                    input(f"\n{Colors.YELLOW}Press Enter to continue...{Colors.ENDC}")
                    
            except KeyboardInterrupt:
                print(f"\n\n{Colors.GREEN}👋 Goodbye!{Colors.ENDC}")
                break
            except Exception as e:
                print(f"{Colors.RED}❌ An error occurred: {e}{Colors.ENDC}")
                input(f"\n{Colors.YELLOW}Press Enter to continue...{Colors.ENDC}")

if __name__ == "__main__":
    # Check if running with appropriate permissions
    if os.name == 'posix' and os.geteuid() != 0:
        print(f"{Colors.YELLOW}⚠️  Warning: Not running as root. Some operations may fail.{Colors.ENDC}")
        response = input(f"{Colors.YELLOW}Continue anyway? (y/N): {Colors.ENDC}")
        if response.lower() != 'y':
            sys.exit(1)
    
    manager = DockerManager()
    manager.run()