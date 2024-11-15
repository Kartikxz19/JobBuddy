import requests
from serpapi.google_search import GoogleSearch
import http.client, urllib.parse  # For Bing search
import json
from typing import List, Dict
import os
from datetime import datetime


class SearchAPIs:
    def __init__(self):
        # Load API keys from environment variables
        self.serpapi_key = os.getenv("SERPAPI_KEY")
        self.bing_key = os.getenv("BING_API_KEY")
        self.base_urls = {
            'leetcode.com': 'site:leetcode.com',
            'geeksforgeeks.org': 'site:geeksforgeeks.org',
            'glassdoor.com': 'site:glassdoor.com'
        }

    def search_with_serpapi(self, query: str, num_results: int = 3) -> List[Dict[str, str]]:
        """
        Search using SerpAPI (Google Search API)
        Free tier: 100 searches/month
        """
        try:
            site_filters = ' OR '.join(self.base_urls.values())
            search_query = f"{query} interview experience ({site_filters})"

            params = {
                "engine": "google",
                "q": search_query,
                "api_key": self.serpapi_key,
                "num": num_results,
                # Filter to only get recent results (last 2 years)
                "tbs": "qdr:y2"
            }

            search = GoogleSearch(params)
            results = search.get_dict()

            if "organic_results" not in results:
                return []

            processed_results = []
            for result in results["organic_results"]:
                processed_results.append({
                    'url': result.get('link', ''),
                    'title': result.get('title', ''),
                    'snippet': result.get('snippet', ''),
                    'source': 'serpapi'
                })

            return processed_results

        except Exception as e:
            print(f"SerpAPI error: {e}")
            return []

    def search_with_bing(self, query: str, num_results: int = 3) -> List[Dict[str, str]]:
        """
        Search using Bing Web Search API
        Free tier: 1000 transactions per month
        """
        try:
            site_filters = ' OR '.join(self.base_urls.values())
            search_query = f"{query} interview experience ({site_filters})"

            headers = {
                'Ocp-Apim-Subscription-Key': self.bing_key
            }

            params = urllib.parse.urlencode({
                'q': search_query,
                'count': num_results,
                'offset': '0',
                'mkt': 'en-US',
                'freshness': 'Year'  # Get results from the past year
            })

            conn = http.client.HTTPSConnection('api.bing.microsoft.com')
            conn.request("GET", f"/v7.0/search?{params}", headers=headers)
            response = conn.getresponse()
            data = json.loads(response.read())

            if "webPages" not in data:
                return []

            processed_results = []
            for result in data['webPages']['value']:
                processed_results.append({
                    'url': result.get('url', ''),
                    'title': result.get('name', ''),
                    'snippet': result.get('snippet', ''),
                    'source': 'bing'
                })

            return processed_results

        except Exception as e:
            print(f"Bing API error: {e}")
            return []

    def cache_results(self, query: str, results: List[Dict[str, str]]):
        """Cache search results to minimize API calls"""
        try:
            cache_file = 'search_cache.json'
            current_time = datetime.now().isoformat()

            # Load existing cache
            if os.path.exists(cache_file):
                with open(cache_file, 'r') as f:
                    cache = json.load(f)
            else:
                cache = {}

            # Add new results to cache
            cache[query] = {
                'timestamp': current_time,
                'results': results
            }

            # Save updated cache
            with open(cache_file, 'w') as f:
                json.dump(cache, f)

        except Exception as e:
            print(f"Caching error: {e}")

    def get_cached_results(self, query: str) -> List[Dict[str, str]]:
        """Get results from cache if available and recent"""
        try:
            cache_file = 'search_cache.json'
            if not os.path.exists(cache_file):
                return None

            with open(cache_file, 'r') as f:
                cache = json.load(f)

            if query not in cache:
                return None

            cached_time = datetime.fromisoformat(cache[query]['timestamp'])
            current_time = datetime.now()

            # Check if cache is less than 24 hours old
            if (current_time - cached_time).days < 1:
                return cache[query]['results']

            return None

        except Exception as e:
            print(f"Cache retrieval error: {e}")
            return None


def search_alternative_sources(self, company: str, role: str) -> List[Dict[str, str]]:
    """Search using multiple search APIs with caching"""
    search_query = f"{company} {role}"

    # Check cache first
    cached_results = self.get_cached_results(search_query)
    if cached_results:
        return cached_results

    results = []

    # Try SerpAPI first
    if self.serpapi_key:
        results = self.search_with_serpapi(search_query)

    # If SerpAPI fails or no results, try Bing
    if not results and self.bing_key:
        results = self.search_with_bing(search_query)

    # Cache the results if we found any
    if results:
        self.cache_results(search_query, results)

    return results

from langchain_community.document_loaders import WebBaseLoader
def fetch_interview_content(url: str):
    try:
        # Initialize WebBaseLoader with the target URL
        loader = WebBaseLoader(url)
        documents = loader.load()

        # The loader returns documents with text content from the webpage
        if documents:
            # Extract text content
            return documents[0].page_content[:6000]  # Limit to 6000 characters if necessary
        return "No relevant content found."

    except Exception as e:
        print(f"Error while loading content with WebBaseLoader: {e}")
        return "Error during scraping."

# Usage example
answer_box_link = "https://www.glassdoor.com/Interview/Google-Test-Engineer-Interview-Questions-EI_IE9079.0,6_KO7,20.htm#:~:text=Successful%20candidates%20are%20invited%20to,system%20design%2C%20and%20behavioral%20questions.&text=Describe%20a%20situation%20where%20you,debugging%20and%20resolving%20the%20issue%3F"
content = fetch_interview_content(answer_box_link)
print(content)