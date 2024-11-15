import os
import subprocess
try:
    result = subprocess.run(["pdflatex", "--version"], check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    print(result.stdout)
except FileNotFoundError as e:
    print(f"Error: {e}")