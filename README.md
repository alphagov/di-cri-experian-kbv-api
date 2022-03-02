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

## Deploy to AWS lambda

Automated GitHub actions deployments to di-ipv-cri-dev have been enabled for this repository.

The automated deployments are triggered on a push to main after PR approval.

GitHub secrets are required which must be configured in an environment for security reasons.

Required GitHub secrets:

| Secret | Description |
| ------ | ----------- |
| AWS_ROLE_ARN | Assumed role IAM ARN |
| AWS_PROFILE_PATH | Parameter Store path to the signing profile versioned ARN |
| AWS_ROLE_SESSION | Assumed Role Session ID
