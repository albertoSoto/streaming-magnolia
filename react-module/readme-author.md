#Static website
https://www.gatsbyjs.org/features/#static-content

#Fetch basic login example


```javascript
let username = 'test-name';
let password = 'EbQZB37gbS2yEsfs';
let formdata = new FormData();
let headers = new Headers();

formdata.append('grant_type','password');
formdata.append('username','testname');
formdata.append('password','qawsedrf');
headers.append('Authorization', 'Basic ' + base64.encode(username + ":" + password));
fetch('https://www.example.com/token.php', {
    method: 'POST',
    headers: headers,
    body: formdata
}).then((response) => response.json())
    .then((responseJson) => {
        console.log(responseJson);
        this.setState({data: responseJson})
  }).catch((error) => {
        console.error(error);
});
```

