<h1 align="center">Divvy Gen</h1>
<div align="center">
	<strong>Generate Virtual Cards using Divvy</strong>
</div>
<br />

# Reason
My reason for open sourcing this, I made this gen as free lance dev work but since AYCD added it there is no point in keeping
it private. Enjoy :)

# Basic Config
```json
{
  "identifications": {
    "owner_id": "USER_ID",
    "company_id": "COMPANY_ID"
  },
  "account": {
    "email": "EMAIL",
    "password": "PASSWORD"
  },
  "card_settings": {
    "name": "NAME_OF_CARDS"
  },
  "settings": {
    "time_out": TIMEOUT
  }
}
```

# Recommendations
I recommend 5-10 second delay to avoid account getting locked for generating to many cards

# Usage
To use go to [Latest Release](https://github.com/skateboard/divvy-gen/releases/tag/1.0) and download.
once downloaded make sure you have a config in the same folder called ```config.json``` with the config format as seen above. Simply run this
```
java -jar divvy-gen.jar
```
and follow the steps it asks for
