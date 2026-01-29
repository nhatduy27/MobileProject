#!/bin/bash

# Test GPS create trip with LocationDto serialization fix

TOKEN="eyJhbGciOiJSUzI1NiIsImtpZCI6IjFjMzIxOTgzNGRhNTBlMjBmYWVhZWE3Yzg2Y2U3YjU1MzhmMTdiZTEiLCJ0eXAiOiJKV1QifQ.eyJuYW1lIjoiTMOqIFThuqVuIFNoaXBwZXIiLCJyb2xlIjoiU0hJUFBFUiIsImlzcyI6Imh0dHBzOi8vc2VjdXJldG9rZW4uZ29vZ2xlLmNvbS9mb29kYXBwcHJvamVjdC03YzEzNiIsImF1ZCI6ImZvb2RhcHBwcm9qZWN0LTdjMTM2IiwiYXV0aF90aW1lIjoxNzY5NjU2ODQ5LCJ1c2VyX2lkIjoiNjg1MnZHZVIzcVVqTE9hSGx6VGJxV2pmV1BUMiIsInN1YiI6IjY4NTJ2R2VSM3FVakxPYUhselRicVdqZldQVDIiLCJpYXQiOjE3Njk2NTY4NDksImV4cCI6MTc2OTY2MDQ0OSwiZW1haWwiOiJoaWVwc2hpcHBlckBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInBob25lX251bWJlciI6Iis4NDkwMTIzNDU0MiIsImZpcmViYXNlIjp7ImlkZW50aXRpZXMiOnsiZW1haWwiOlsiaGllcHNoaXBwZXJAZ21haWwuY29tIl0sInBob25lIjpbIis4NDkwMTIzNDU0MiJdfSwic2lnbl9pbl9wcm92aWRlciI6ImN1c3RvbSJ9fQ.mF5EMZg-Ca24ZaALCcivnhC87aVuGCU4wwDOs8Vpl0T4WkUNf9sC02P8Yo3OQSLUVvHSRH_lMVRMy4oaOEboLk_u3y3v5MZeJAZ5M6pQGCHwImsRsQu9aeY3djZDzOdtT5bkYWWW8Bi0NhW_XsIYOP5Z2kuG05-8W2TguoTjCXiCkXfEkzkriBo-feHc91pCdIBMnSdmPiJMkbVitMfdqc2IxFuKxzSkmYYDzuFN-CCfcOAlmaTmoQ8BMCwRnJSK8E_kH7ZpYEcnC244rkEdkh6FL7m39Lw8m57wnq6nIuei83PWe23e253grWT1vIIOSTurvp8ecEWHuASiPAD0Og"

curl -X POST http://localhost:3000/api/gps/create-optimized-trip \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderIds": ["nhEEgpGlWXPNYagMCSnW", "elBmiBLnBrqgYAkhwb79"],
    "origin": {
      "lat": 10.882470,
      "lng": 106.782317,
      "name": "Cổng chính KTX"
    },
    "returnTo": {
      "lat": 10.882470,
      "lng": 106.782317,
      "name": "Cổng chính KTX"
    }
  }'
