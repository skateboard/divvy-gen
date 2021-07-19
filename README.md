<h1 align="center">Divvy Gen</h1>
<div align="center">
	<strong>Generate Virtual Cards using Divvy</strong>
</div>
<br />

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
