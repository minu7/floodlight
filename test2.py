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

payload = "{\n\t\"chain\": [\"nf1.1\"]\n}"

response = requests.request("POST", url, data=payload)

print(response.text)
c = response.text

"""
Associate chain corretta
"""

url = "http://localhost:8080/wm/nfchain/associate"

payload = "{{\n\t\"sourceIp\": \"10.0.0.2\",\n\t\"destIp\": \"10.0.0.3\",\n\t\"nfChainId\": {}\n}}".format(c)

response = requests.request("PUT", url, data=payload)

print(response.text)
