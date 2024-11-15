#experience_scraper.py
import requests
from bs4 import BeautifulSoup
from urllib.parse import quote
import re
from typing import List, Dict
import time
import json
from fake_useragent import UserAgent
import os
from langchain_groq import ChatGroq
import certifi
import urllib3
from serpapi.google_search import GoogleSearch
from langchain.document_loaders import WebBaseLoader


llm = ChatGroq(
    model="llama-3.1-70b-versatile",
    temperature=0,
    groq_api_key=os.getenv("GROQ_API_KEY"),
)



http = urllib3.PoolManager(
    cert_reqs="CERT_REQUIRED",
    ca_certs=certifi.where()
)
class InterviewExperienceScraper:
    def __init__(self):
        self.ua = UserAgent()
        self.headers = {
            'User-Agent': self.ua.random,
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'en-US,en;q=0.5',
            'Accept-Encoding': 'gzip, deflate, br',
            'Connection': 'keep-alive',
            'Upgrade-Insecure-Requests': '1',
            'Cache-Control': 'max-age=0'
        }
        self.serpapi_key = os.getenv("SERPAPI_KEY")
        # Define target sites and their content selectors
        self.target_sites = {
            'leetcode.com': {
                'url': 'https://leetcode.com/discuss/interview-experience/',
                'search_path': 'search?q=',
                'content_selectors': ['.topic-content', '.post-content', '.content-area']
            },
            'geeksforgeeks.org': {
                'url': 'https://www.geeksforgeeks.org/company-interview-corner/',
                'search_path': '?s=',
                'content_selectors': ['.entry-content', '.article-text']
            },
            'glassdoor.com': {  # New entry for Glassdoor
                'url': 'https://www.glassdoor.com/Interview/index.htm',
                'search_path': '?keyword=',  # Adjust if necessary
                'content_selectors': ['.InterviewList']  # Adjust based on actual HTML structure
            }
        }

    def clean_text(self, text: str) -> str:
        """Clean scraped text by removing extra whitespace and unwanted characters."""
        text = re.sub(r'<[^>]+>', '', text)
        text = re.sub(r'\s+', ' ', text)
        text = re.sub(r'[^\w\s.,?!-]', '', text)
        return text.strip()

    def direct_site_search(self, company: str, role: str) -> List[Dict[str, str]]:
        """Search directly on target sites instead of using Google."""
        results = []

        for site_name, site_info in self.target_sites.items():
            try:
                # Create search URL for the specific site
                if site_name == "glassdoor.com":
                    search_term = f"{company} {role} interview"
                    search_url = f"{site_info['url']}{site_info['search_path']}{quote(search_term)}"
                else:
                    search_term = f"{company} {role} interview experience"
                    search_url = f"{site_info['url']}{site_info['search_path']}{quote(search_term)}"

                print(f"Searching {site_name} at URL: {search_url}")  # Debug print
                print(http.request('GET', search_url, headers=self.headers))
                response = requests.get(
                    search_url,
                    headers=self.headers,
                    timeout=15,
                    verify=False  # Only if necessary for SSL issues
                )

                if response.status_code == 200:
                    soup = BeautifulSoup(response.text, 'html.parser')

                    # Try each content selector for the site
                    for selector in site_info['content_selectors']:
                        content_elements = soup.select(selector)

                        for element in content_elements[:2]:  # Limit to top 2 results per site
                            content = self.clean_text(element.get_text())
                            if len(content) > 100:  # Minimum content threshold
                                results.append({
                                    'url': search_url,
                                    'site': site_name,
                                    'content': content[:2000]  # Limit content length
                                })
                                break  # Found content, move to next selector

                time.sleep(2)  # Polite delay between requests

            except Exception as e:
                print(f"Error scraping {site_name}: {e}")
                continue

        return results

    def search_alternative_sources(self, company: str, role: str) -> List[Dict[str, str]]:
        """Search using alternative sources when direct site search fails."""

        try:

            # Example: Using a public API (you'll need to sign up for an API key)
            # This is just an example - replace with actual API endpoints
            params = {
                "api_key": "f3ae9b85ed5da0fc5c961f3060ad8edd3d6aa55475dee5365f50e0258f7e0eba",
                "engine": "google",
                "q": f"{company} {role} interview experiences",
                "google_domain": "google.com",
                "gl": "us",
                "hl": "en"
            }

            search = GoogleSearch(params)
            data = search.get_dict()
            related_questions = []
            if 'related_questions' in data:
                for question in data['related_questions']:
                    related_questions.append({
                        'question': question['question'],
                        'snippet': question['snippet'],
                        'link': question['link'],
                        'title': question['title'],
                        'displayed_link': question['displayed_link']
                    })

                # Extract answer box information
            answer_box = {}
            if 'answer_box' in data:
                answer_box = {
                    'title': data['answer_box'].get('title', ''),
                    'link': data['answer_box'].get('link', ''),
                    'snippet': data['answer_box'].get('snippet', ''),
                    'displayed_link': data['answer_box'].get('displayed_link', '')
                }

            return {
                'related_questions': related_questions,
                'answer_box': answer_box
            }
        except Exception as e:
            print(f"Error with alternative source: {e}")
            return []

    def get_mock_data(self, company: str, role: str) -> List[Dict[str, str]]:
        """Provide mock data when all other methods fail."""
        return [{
            'url': 'mock_url',
            'site': 'Mock Data',
            'content': f"""
            Common interview process for {role} at {company}:
            1. Initial HR screening
            2. Technical assessment
            3. System design discussion
            4. Behavioral interviews

            Common questions include data structures, algorithms, and past project experiences.
            The process typically takes 2-3 weeks.
            """
        }]

    def search_interview_experiences(self, company: str, role: str) -> List[Dict[str, str]]:
        """Main search function that tries multiple methods."""
        print(f"Searching for {company} {role} interview experiences...")  # Debug print

        # Try direct site search first
        #results = self.direct_site_search(company, role)

        # If direct search yields no results, try alternative sources
        #if not results:
            #print("Direct search yielded no results, trying alternative sources...")
        results = self.search_alternative_sources(company, role)
        print(results)
        answer_box_link = results.get('answer_box', {}).get('link')
        try:
            # Make a request to the link
            headers = {
                "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36",
                "Referer": "https://www.google.com/",
                "Accept-Language": "en-US,en;q=0.9",
                "Cookie":"gdId=b31736ca-d633-49d9-83f8-e98f1d295c89; rl_page_init_referrer=RudderEncrypt%3AU2FsdGVkX1%2F4kpuGmnZkupQGynn%2B%2F%2BGe6r4XtYfMXMFKbNjE7p33O98E2VQxHQTi; rl_page_init_referring_domain=RudderEncrypt%3AU2FsdGVkX18ii%2BDP3EsDJkkpnEW9Tb4aDt%2BqPLlWpXU%3D; _ga=GA1.3.124007450.1721976982; _optionalConsent=true; _fbp=fb.2.1721976983397.762644190978401918; uc=8013A8318C98C517C045AF3A87F545C8156250C6E81E6C2065E0407CD09A298CF5354B8B4784F70D8B3894A182E3E677541D623AD06B10F6FE6687A74273ACD4784F33657D85ADE973EA879D32EE3C497CB75F199B6F2BE5B775F193E173D85EF40F2E6143DAC48800E56C180568CB8E5822737AC0563B90F5734F6FDF69EC8118311CF89BD9C79509D4E58F612281E6; rttdf=true; indeedCtk=1i7u73449h755802; ki_s=240461%3A0.0.0.0.0; _ga_500H17GZ8D=GS1.3.1728457797.1.0.1728457797.60.0.0; cf_clearance=Mm8z4vTIT.6B910ghEndzYwx.6UjoYcrWb4q.OJ1nUI-1728457796-1.2.1.1-ZyAV3f23n1aHfWki97Cqo2IHg6G2pm4omCx6iP_j_Okjwk88GLMcOuuQfDcy8FdTcwd3hyCSNo.g.AnbYoEH1Ug_taGTrQnNE2Wd9tqd7nQnkbASxftkn_kc7Uh0lfXz0Ncj5Egrp_J2kByZiPRIrpbXcVTgzaCv6oXYDUSsM_qzfHfQ98bvCU_b89SaueQwJ_4o9pnMyhnHxTAVFpZjdo01spi1eEnHwatLX4hR7Tell9mHyym6fyTTKHNuhB34Q4j4KpNObqQrqnfAU0ZD8ctKrsSzh9AkQIL74AnU3teadr0pGvC8_LZtwtIbeQfAvAlRflnsaC1yG3jrOP6g5g9hFvNoZV8_y.6IYHZo6Psenr6iTf49nNGUVUtCDFPz.yYMjze5zq8UtRPy3Vldq8o5mVRNXvTBZnKqNhZmWrVhflqQM6I4fUSMAl4eg._W; _gcl_au=1.1.418012831.1730456891; _cfuvid=gTNDghsDCHK5wuhXugI_XVxC0erbMbDNtEFRqoe3tfQ-1731642647998-0.0.1.1-604800000; GSESSIONID=undefined; cass=0; gdsid=1731642647479:1731647113553:AE9B89488260DFC5BE2417523081D8B3; at=v2V7QhS9LySQX-iSV7sw2RhkGBR858rYfTxEdJP_foB-ETQ9FOx_wpTqqUKa2vScREvX35BFbUZB7CyUXF4iZ5lVjHbejKK5-94Nf4vaWVMmw20kr_j4JEM3XptZJi-cAgbajybRSko5sq9hz6e8Gmuyb3lQFk06VS1gIaLOkHB1izgTQTEx7TB-WKxyXdE0yU8XvJd62hVPVHjxjmG4gxy5KSsLBobTDy8k4rSICq9JGYAGPKiHSB6-bX2gVAwwpQ5BvQ4WBM-OzkChU2XYbZul4Tg8bGiw0dJbLAyZnfCHscvpdjJ3uOBgBVAjzQjCeTbtHwo1x3S3k8QqH_yHJvZ8pmAMgH_I_d32R1deMZxlxW3k1lrL3L28a5MUT_C4QDpP1lGshr1yyLSRBh-AgRx-YNVaZn1G-3Qm9LuD1ra8-pLGAvdY1diToYYAQsLGQN9DZKgT7AhV9aWEeaUJJ1bwuRr0aCB0BhR0IJ_fDGPubBaxZle8g_2GkJv1HmvfsObAy_AC2UnplIvn8CFH6qRYAviYUzCobtnTiMY-jLK9r1Fj-J2WuU2MqowsGgFe6KgdV7paZQcsZain_W8e8_tQivSQq3k4ZpSWmU2x03KcCsPy7heSgl7A-1i2FvRzqwRTo0468P0v4Gqu6B3emKMEvK-tzMGL0cc48NSMC3RjCanJOzadoEv7q0OzO7kPg7H79Cr53uoEJzlyPsr42VZ5ssXfsJKOLr_mSxPHosfJD2rKaJsI3ZauQP2JT6Hro2zPCqAZOM2zppMNJ8zuFhmJBxoEfSv8_RXO7ig6toVcyJCyW3a3jF7VgT5zdqgtaIWWLIp9ZzX7hgH0Xh20LBicZ4X1KliEyHmZPX494xJMWFQGz4UqXcnfaF6MJ4_PKeNr; asst=1731647113.0; bs=QtfYJKRZ4j4lgmGi_owUrQ:G3xwFOfr0oaraTWfS_C831n8R32mI3BS8Vh35d8nud8l1-JfI438YV-suvih4CJzuCIOO7CriUjzhGGjdzMwYhDG4ZGOYFAPUoW-3XM_SmQ:YqzD_VrAT4g1EpvbImLIFvZsNKYb99JgAxBVRIU7kCA; __cf_bm=kS_Wgf49dfTJhDanKvDefIF2mOsPRei9ooBnwgGB7kE-1731647114-1.0.1.1-LV1HZt.59boR_SA3tT_kQN7FeZb1ppDIMuu1HRwThS4wbUTH_HLwVWaFhOHD8NRp.AVv.l9Qgk_D02swr0AyJA; rl_user_id=RudderEncrypt%3AU2FsdGVkX18zaAYtnJ2htEB61qDqHXpuG1mkp2fKgjY%3D; rl_trait=RudderEncrypt%3AU2FsdGVkX1%2FZmT340skxX1rzQX%2Bz%2F25l1tV2RgcamxK9jobzKwkvVhnhZYwHFaqRrMGmIocaxXaW3WBi2GV%2FHTXYengqex5rgpr6MIZ7jPp9yCMFWjL%2B25OQEpx8OZMVqLpjlG15VXPgULA6SsBHe2ccIKvQxZP11%2BNaEyaSD3Udr05De91G57brLvu%2BlT3V9IE7BeaPj%2FFdMFQbnRsjoiV98JIiLFR06vh0LILgwaY%3D; rl_group_id=RudderEncrypt%3AU2FsdGVkX1%2FNB9sxokdpcjT7hMaZalSKok1G3E5Gnn8%3D; rl_group_trait=RudderEncrypt%3AU2FsdGVkX1%2Fmavqr%2FXaNPM6f5HXbQDHhAQjZ7Iy8lA0%3D; rl_anonymous_id=RudderEncrypt%3AU2FsdGVkX1%2FaRHT5%2BNBrlIgSIMD0Ncl982Skqk3ZzHomanoskgeovNtieawBEbpM62XavALypF57zXuh5gt7yQ%3D%3D; rsReferrerData=%7B%22currentPageRollup%22%3A%22%2Finterview%2Finterview-questions%22%2C%22previousPageRollup%22%3Anull%2C%22currentPageAbstract%22%3A%22%2FInterview%2F%5BEMP%5D-%5BOCC%5D-Interview-Questions-EI_%5BPRM%5D.htm%22%2C%22previousPageAbstract%22%3Anull%2C%22currentPageFull%22%3A%22https%3A%2F%2Fwww.glassdoor.co.in%2FInterview%2FGoogle-Test-Engineer-Interview-Questions-EI_IE9079.0%2C6_KO7%2C20.htm%3FcountryRedirect%3Dtrue%22%2C%22previousPageFull%22%3Anull%7D; rsSessionId=1731647116235; OptanonConsent=isGpcEnabled=0&datestamp=Fri+Nov+15+2024+10%3A35%3A18+GMT%2B0530+(India+Standard+Time)&version=202407.2.0&browserGpcFlag=0&isIABGlobal=false&hosts=&consentId=88cdae33-3e51-422c-bb5f-ea9fd5b03d4d&interactionCount=1&isAnonUser=1&landingPath=NotLandingPage&groups=C0001%3A1%2CC0003%3A1%2CC0002%3A1%2CC0004%3A1%2CC0017%3A1&AwaitingReconsent=false; ki_t=1728119905388%3B1731642653723%3B1731647119795%3B5%3B9; ki_r=; AWSALB=9z2CECRMSSmtbeaF39dcAuTc1UdLbv7RclMU+V360FuK34GbztP2ecoBC4x7EtBrsdXkc8Jv7GFVqAANkofuzECpFnR/hFvZ3ehr7KgHg8NMzQBuaovbC2lixHZM; AWSALBCORS=9z2CECRMSSmtbeaF39dcAuTc1UdLbv7RclMU+V360FuK34GbztP2ecoBC4x7EtBrsdXkc8Jv7GFVqAANkofuzECpFnR/hFvZ3ehr7KgHg8NMzQBuaovbC2lixHZM; JSESSIONID=695A280AFC2B285E78DDACABDBE2C329; rl_session=RudderEncrypt%3AU2FsdGVkX1%2BouMV9mOJUgwwDsZyjxmEsrqFE4pdEdYM90LrF6qjFoJmLQwxXY1NbESOGLVC%2Bth%2BweAldMCF50q72o0sYTLw0PdiBIOlSoESkdoKmgLd8t8ZffX6xFxHQuKeUvlngsUnfi1Rn697U9w%3D%3D; cdArr=72%2C64%2C59%2C63"
            }
            loader = WebBaseLoader(answer_box_link)
            documents = loader.load()

            # The loader returns documents with text content from the webpage
            if documents:
                # Extract text content
                return documents[0].page_content[:6000]  # Limit to 6000 characters if necessary
            return "No relevant content found."

        except Exception as e:
            print(f"Error while scraping content: {e}")
            return "Error during scraping."
        # If still no results, return mock data
        #if not results:
            #print("No results found, returning mock data...")
            #results = self.get_mock_data(company, role)

        return results

    def summarize_experiences(self, experiences: List[Dict[str, str]]) -> str:
        """Summarize the collected interview experiences using the LLM."""
        if not experiences:
            return "No interview experiences found."

        # Prepare the text for summarization
        combined_text = "\n\n".join([
            f"Source: {exp['site']}\n{exp['content']}"
            for exp in experiences
        ])

        prompt = f"""
        Summarize these interview experiences concisely, focusing on:
        1. Common interview questions and topics
        2. Interview process steps
        3. Key technical skills tested
        4. Important preparation tips

        Interview Experiences:
        {combined_text}

        Provide a structured summary in under 300 words.
        """

        response = llm.invoke(prompt)
        return response.content


def get_interview_insights(job_posting: dict) -> str:
    """Main function to get interview insights based on job posting."""
    scraper = InterviewExperienceScraper()

    # Extract company and role from job posting
    company = job_posting.get('company', '')
    role = job_posting.get('role', '')
    print(job_posting)
    if not company or not role:
        return "Could not determine company or role from job posting."

    # Get interview experiences
    experiences = scraper.search_interview_experiences(company, role)
    print(experiences)
    # Summarize experiences
    summary = scraper.summarize_experiences(experiences)

    return summary