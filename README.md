# Simple Application for Replicating MOEX Indexes

This application is designed for the independent replication of the Moscow Exchange Index (IMOEX) or the Blue Chip Index (MOEX-10). The passive index investing strategy involves regular portfolio rebalancing, which can occur every three months or even less frequently.

## Key Features

- **Data Retrieval from MOEX API**: The application uses the MOEX API to obtain up-to-date information about index compositions and current stock prices. Prices and index compositions are updated upon user request, ensuring the information is current.

- **Custom Coefficient Settings**: Users can set their own coefficients for each ticker, allowing for portfolio customization based on individual investment strategies.

- **Adding Tickers**: The ability to add tickers not included in the index provides users with flexibility in portfolio construction.

- **Distribution Amount Input**: Users can specify the desired amount for distribution and the number of shares purchased. The application automatically calculates how to allocate the specified amount among tickers based on their weight, price, and lot size.

- **Purchase Recommendations**: The application indicates how many shares and for what amount need to be purchased to achieve the desired distribution.

- **Compliance Percentage**: After the user fills in the number of shares purchased, the application calculates the compliance percentage, allowing users to assess how closely their portfolio matches the specified parameters.

## Installation and Setup

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/koster18/IMOEX-screener/
   cd IMOEX-screener

2. **Install Dependencies**:

   docker-compose up

3. **Run the Application**:

   http://localhost:8181/stocks

## Usage
After launching the application, users will be provided with an interface for portfolio configuration. Enter the required data, set coefficients, and follow the on-screen instructions to receive stock purchase recommendations.

## Contribution
If you would like to contribute to the project, please create a merge request or open an issue in the repository.

## License
This project is licensed under the Unlicense. 