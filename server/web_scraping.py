from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager

# Set up Chrome options to mimic a real browser
chrome_options = Options()

# Set a realistic User-Agent to mimic a real browser
chrome_options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")

# Disable automation flags (to prevent detection)
chrome_options.add_argument("--disable-blink-features=AutomationControlled")

# Make the browser window look like a real one (optional)
chrome_options.add_argument("--window-size=1920x1080")

# Use the WebDriver Manager to handle driver download automatically
driver = webdriver.Chrome(executable_path=ChromeDriverManager().install(), options=chrome_options)


# Example to visit a webpage
driver.get("https://www.glassdoor.co.in/Interview/Google-Test-Engineer-Interview-Questions-EI_IE9079.0,6_KO7,20.htm?countryRedirect=true")

# Wait for a few seconds to simulate human-like browsing
driver.implicitly_wait(5)

# Perform interactions as a real user (optional)
# driver.find_element_by_name('q').send_keys("Selenium")

# Print the page title
print(driver.title)

# Close the browser
driver.quit()

