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
		`@bot remind me 'what' at 16/03/2019 16:33`  
	b) For anyone in the current room   
		`@bot remind @George Papakis 'what' at 16/03/2019 16:33`  
	c) All in the current room  
		`@bot remind @all 'what' at 16/03/2019 16:33`  
	d) All in any other room that bot is invited    
		`@bot remind #roomName 'what' at 16/03/2019 16:33`  
		
2) Set timezone  
	a) For each reminder   
		`@bot remind me 'what' at 16/03/2019 16:33 Athens `  
	b) If previews omitted set timezone for each user in every reminder he sets  
		`@bot set my timezone to athens`  
	c) If previews omitted set timezone for every user in the current domain  
		`@bot set global timezone to Paris`  
	d) By default it uses GMT
	
3) Show my reminders and timezone                                                                              
a) For each user shows reminders that will notify him.  
        `@bot list`       
          Example:  
         `1) ID:23 what:' Something to do ' When: 23/01/2019 18:20 Europe/Athens`  
         b) To show your timezone and global timezone simply do     
         `@bot timezones`  
4) Delete a reminder  
    a) For each user, using a reminders id.  
        `@bot delete 323 `     
5) Show current version of the bot                                  
    a) For each user, using a reminder version.  
        `@bot version`          
 