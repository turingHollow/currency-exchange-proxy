# This project is a programming assignment for a job interview.

## Initial assignment

The goal is to build a simple application that acts as a local proxy for getting exchange rates.
It's a service that can be consumed by other internal services to get the exchange rate between a set of currencies,
so they don't have to care about the specifics of third-party providers.

The following use case must be addressed:

The service returns an exchange rate when provided with 2 supported currencies
The rate should not be older than 5 minutes
The service should support at least 10,000 successful requests per day with 1 API token

Please note the following drawback of the One-Frame service:
The One-Frame service supports a maximum of 1000 requests per day for any given authentication token.

## Personal observations

1) It would be possible to reverse a pair of currencies and infer the pair (B -> A) from the pair (A -> B). For example:
   Original quote (EUR → USD):
   bid = 0.8175016986329343  (sell EUR, get USD)
   ask = 0.4125117613577517  (buy EUR, pay USD)
   Calculated (USD → EUR):
   bid = 1 / 0.4125117613577517 ≈ 2.424
   ask = 1 / 0.8175016986329343 ≈ 1.224
   But one-frame api generate completely random data and doesn't support this constraint.

2) List of supported currencies in the application scaffold if far smaller than One-Frame api supports.

Initial app supports:
Enum (AUD, CAD, CHF, EUR, GBP, NZD, JPY, SGD, USD) - 9 currencies , there only 36 unique pairs. Or 72 ordered pairs.

One-Frame api supports.
Enum (AED, AFN, ALL, AMD, ANG, AOA, ARS, AUD, AWG, AZN, BAM, BBD, BDT, BGN, BHD, BIF, BMD, BND, BOB, BRL, BSD, BTN,
BWP, BYN, BZD, CAD, CDF, CHF, CLP, CNY, COP, CRC, CUC, CUP, CVE, CZK, DJF, DKK, DOP, DZD, EGP, ERN, ETB, EUR, FJD,
FKP, GBP, GEL, GGP, GHS, GIP, GMD, GNF, GTQ, GYD, HKD, HNL, HRK, HTG, HUF, IDR, ILS, IMP, INR, IQD, IRR, ISK, JEP,
JMD, JOD, JPY, KES, KGS, KHR, KMF, KPW, KRW, KWD, KYD, KZT, LAK, LBP, LKR, LRD, LSL, LYD, MAD, MDL, MGA, MKD, MMK,
MNT, MOP, MRU, MUR, MVR, MWK, MXN, MYR, MZN, NAD, NGN, NIO, NOK, NPR, NZD, OMR, PAB, PEN, PGK, PHP, PKR, PLN, PYG,
QAR, RON, RSD, RUB, RWF, SAR, SBD, SCR, SDG, SEK, SGD, SHP, SLL, SOS, SPL, SRD, STN, SVC, SYP, SZL, THB, TJS, TMT,
TND, TOP, TRY, TTD, TVD, TWD, TZS, UAH, UGX, USD, UYU, UZS, VEF, VND, VUV, WST, XAF, XCD, XDR, XOF, XPF, YER, ZAR,
ZMW, ZWD) - 161 currencies, there are 12800 unique pairs.

## Simple caching solution (small currency set)

For a small, fixed set of currencies, the proxy can retrieve all pair quotes for that set in one shot and fully refresh
an in-memory cache whenever data is needed, honoring a 5-minute TTL. This approach is straightforward and keeps all
pairs fresh together, as the provider’s api batch endpoint and the daily quota allow it.

## More complex solution to support a large set of currencies.

As it is not possible to retrieve the whole list of rates for a large set a possible solution is to retrieve all pairs
with USD and
compute the rest of the rates on demand, using cross-rate calculation.
You can get "JPY" → "GBP" if you know:
"JPY" → "USD"
"USD" → "GBP"
Let’s pretend we have:
JPY→USD: bid = 0.0068, ask = 0.0069
USD→GBP: bid = 0.7900, ask = 0.7910
Then:
JPY→GBP bid = 0.0068 × 0.7900 = 0.005372
JPY→GBP ask = 0.0069 × 0.7910 = 0.005468
So it is possible to compute all pairs using only information about all 160 pairs with USD.

## Implementation

For now, I'm going to use a simple caching solution. But can implement the more complex solution if needed.

I used a simple Map wrapped in a Ref to ensure safe, concurrent data access and updates.
I separated the logic for serving data from the cache and the logic for updating the cache into two distinct components.
This separation of concerns makes the system easier to reason about and also enables the possibility of deploying these
parts of the application independently in the future for better scalability.

## Local run

Start docker container with OneFrame

pull the image
`docker pull paidyinc/one-frame:latest`

run the service
`docker run -p 8080:8080 paidyinc/one-frame`

run app
`sbt run`

send request to app
`curl 'localhost:8090/rates?from=USD&to=JPY'`

