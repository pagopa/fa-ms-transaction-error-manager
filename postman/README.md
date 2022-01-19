# Postman

This directory contains Postman collections intended to be run as end-to-end integration tests during the deployment
pipeline of the project.

# Running a collection with Newman

```bash
newman run <COLLECTION> -e <ENVIRONMENT>
```

# Variables expected to be defined in the collection environment

| Variable | Description |
| ------------- | ------------- |
| `cv_provider_id`  | A valid provider Id |
| `cv_contract_id` | A valid contract Id |
| `cv_shop_id` | A valid shop Id |
| `cv_trx_date` | A valid transaction date |
| `cv_initial_amount` | A valid amount |