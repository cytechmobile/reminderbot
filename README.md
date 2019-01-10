## What it is.  
   As indicates its name this project is a reminder bot. Its created to use in Google Chat using REST API. 

## For who.  
   Anyone that uses Google Chat.

## What is needed to build and use it.

   1) A **google service account** and a **private key**. More info [here](https://developers.google.com/hangouts/chat/how-tos/service-accounts) and [here](https://developers.google.com/hangouts/chat/how-tos/bots-publish).  
   2) A **server** to host it and a **database**.  
		As environment variables needs :   
		a) Bot name  
		b) Path of the private key	  
		c) Credentials for the connection with database  
## How to use it.  
  The current functionality that this bot supports after you invite it is the following:
1)  Set a reminder  
	a) For you   
		`@bot reminder me 'what' at 16/3/2019 16:33`  
	b) For anyone in the current room   
		`@bot reminder @George Papakis 'what' at 16/3/2019 16:33`  
	c) All in any the current room  
		`@bot reminder @all 'what' at 16/3/2019 16:33`  
	d) All in any other room that bot is invited    
		`@bot reminder #roomName 'what' at 16/3/2019 16:33`  
		
2) Set timezone  
	a) For each reminder   
		`@bot reminder me 'what' at 16/3/2019 16:33 Athens `  
	b) If previews omitted set timezone for each user in every reminder he sets  
		`@bot mytimezone London `  
	c) If previews omitted set timezone for every user in the current domain  
		`@bot timezone Paris`  
	d) By default it uses GMT
	
3) Show my reminders  
    a) For each user that reminders will notify him.  
        `@bot myreminders`       
4) Delete a reminder  
    a) For each user, using a reminders id.  
        `@bot delete 323 `     