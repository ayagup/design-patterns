#!/usr/bin/env python3
"""
Verification Script - Tests a sample pattern from each category
This ensures all patterns are properly formatted and executable
"""

import subprocess
import sys
from pathlib import Path

# Sample patterns to test from each category
TEST_PATTERNS = {
    "Creational": "creational/singleton_pattern.py",
    "Structural": "structural/adapter_pattern.py",
    "Behavioral": "behavioral/observer_pattern.py",
    "Concurrency": "concurrency/thread_pool_pattern.py",
    "Architectural": "architectural/mvc_pattern.py",
    "Enterprise": "enterprise/repository_pattern.py",
    "Cloud": "cloud/circuit_breaker_pattern.py",
    "Microservices": "microservices/api_gateway_pattern.py",
}

def test_pattern(pattern_path):
    """Test if a pattern executes successfully"""
    try:
        result = subprocess.run(
            [sys.executable, pattern_path],
            capture_output=True,
            text=True,
            timeout=5
        )
        return result.returncode == 0, result.stdout, result.stderr
    except subprocess.TimeoutExpired:
        return False, "", "Timeout"
    except Exception as e:
        return False, "", str(e)

def main():
    """Run verification tests"""
    print("\n" + "="*80)
    print("DESIGN PATTERNS VERIFICATION")
    print("Testing sample patterns from each category")
    print("="*80 + "\n")
    
    base_path = Path(__file__).parent
    passed = 0
    failed = 0
    
    for category, pattern_path in TEST_PATTERNS.items():
        full_path = base_path / pattern_path
        
        if not full_path.exists():
            print(f"❌ {category:15} - FILE NOT FOUND: {pattern_path}")
            failed += 1
            continue
        
        success, stdout, stderr = test_pattern(full_path)
        
        if success:
            print(f"✅ {category:15} - {pattern_path}")
            passed += 1
        else:
            print(f"❌ {category:15} - {pattern_path}")
            if stderr:
                print(f"   Error: {stderr[:100]}")
            failed += 1
    
    print("\n" + "="*80)
    print(f"RESULTS: {passed} passed, {failed} failed out of {len(TEST_PATTERNS)} tests")
    print("="*80 + "\n")
    
    if failed == 0:
        print("🎉 ALL TESTS PASSED! All pattern categories are working correctly.\n")
        return 0
    else:
        print(f"⚠️  {failed} test(s) failed. Please check the errors above.\n")
        return 1

if __name__ == "__main__":
    sys.exit(main())
