import requests

"""
Create NFs on switches
"""
url = "http://localhost:8080/wm/nfchain/nf"

for i in range(1, 7):
  payload = "{{\n\t\"nf\": \"nf{0}\",\n\t\"sw\": \"00:00:00:00:00:00:00:0{0}\"\n}}".format(i)
  print(payload)
  response = requests.request("POST", url, data=payload)
  print(response.text)
  # nell'ultimo la risposta è vuota perchè cerchiamo di creare una NF su uno switch inesistente

payload = "{\n\t\"nf\": \"nf1.1\",\n\t\"sw\": \"00:00:00:00:00:00:00:01\"\n}" # uno switch può avere più di uno switch
print(payload)
response = requests.request("POST", url, data=payload)
print(response.text)

"""
Create first chain
"""
url = "http://localhost:8080/wm/nfchain/define"

payload = "{\n\t\"chain\": [\"nf1\", \"nf3\", \"nf4\", \"nf2\"]\n}"

response = requests.request("POST", url, data=payload)

print(response.text)


"""
Associate chain corretta
"""

url = "http://localhost:8080/wm/nfchain/associate"

payload = "{\n\t\"sourceIp\": \"10.0.0.1\",\n\t\"destIp\": \"10.0.0.4\",\n\t\"nfChainId\": 0\n}"

response = requests.request("PUT", url, data=payload)

print(response.text)


"""
Associate chain errata (ripassiamo da switch)
"""

url = "http://localhost:8080/wm/nfchain/associate"

payload = "{\n\t\"sourceIp\": \"10.0.0.1\",\n\t\"destIp\": \"10.0.0.3\",\n\t\"nfChainId\": 0\n}"

response = requests.request("PUT", url, data=payload)

print(response.text)



"""
Create second chain
"""
url = "http://localhost:8080/wm/nfchain/define"

payload = "{\n\t\"chain\": [\"nf2\", \"nf4\",  \"nf1.1\", \"nf3\"]\n}"

response = requests.request("POST", url, data=payload)

print(response.text)


"""
Associate seconda chain
"""

url = "http://localhost:8080/wm/nfchain/associate"

payload = "{\n\t\"sourceIp\": \"10.0.0.2\",\n\t\"destIp\": \"10.0.0.3\",\n\t\"nfChainId\": 1\n}"

response = requests.request("PUT", url, data=payload)

print(response.text)
