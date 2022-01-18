# di-cri-experian-kbv-api
API used by Credential Issuers to connect to Experian KBV APIs

## Deploy to PaaS

Set environment variables with:
````
cf set-env di-cri-experian-kbv-api KEYSTORE_PASSWORD <the password>
cf set-env di-cri-experian-kbv-api KEYSTORE $(base64 < ~/path/to/keystore)
````

Then build and push with:
````
./gradlew clean build
cf push
````
