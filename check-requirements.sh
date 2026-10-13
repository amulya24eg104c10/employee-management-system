#!/usr/bin/env bash
echo "===== Employee Management System - requirements check ====="
missing=0
check() { # name, command, version-cmd
  printf "%-22s" "$1"
  if command -v "$2" >/dev/null 2>&1; then echo "OK   -> $($3 2>&1 | head -1)"; else echo "MISSING"; missing=$((missing+1)); fi
}
check "Java (need 17+)" java "java -version"
check "Maven" mvn "mvn -version"
check "Node.js" node "node -v"
check "npm" npm "npm -v"
check "MySQL client" mysql "mysql --version"
echo; echo "--- Ports (8080, 3000 should be free; 3306 used by MySQL) ---"
for p in 8080 3000 3306; do
  if (echo > /dev/tcp/127.0.0.1/$p) >/dev/null 2>&1; then echo "Port $p: IN USE"; else echo "Port $p: free"; fi
done
echo; [ $missing -eq 0 ] && echo "RESULT: all tools found." || echo "RESULT: $missing tool(s) missing - install them and re-run."
