# Portfolio Analysis

## Purpose and motivation

This project analyzes the historical performance of a portfolio of ETFs. I got the idea after reading Garth Turner's blog:

http://www.greaterfool.ca/

In many posts he claims that a balanced portfolio held long enough (I think he says 10 years) will return about 7% annually (e.g. see [this post](http://www.greaterfool.ca/2014/04/25/planning-6/) and [this one too](http://www.greaterfool.ca/2014/05/15/the-millennial-portfolio/)). In [one post](http://www.greaterfool.ca/2014/11/21/trust-4/), he even gave an example of some ETFs one might choose. For these, there is common dates data for only about 5 or 6 years (I think the latest one started some time in 2010). So I decided to see for myself whether these claims were plausible. See below for results.
 

## Requirements

- java 1.8
- You must have the following project working on the machine on which you will run this project:

https://github.com/shafiquejamal/ishares-etf-data-download

The same environment variables, databases, etc. must be set up for both projects, an you must run that one before running this one in order to have all the latest ETF data (NAV, etc.)

## How to use

Choose your parameters and files.

- You must design your portfolio, and create a "portfolio design" csv file based on this. A sample exists at:
 
https://raw.githubusercontent.com/shafiquejamal/portfolioanalysis/master/src/test/scala/com/eigenroute/portfolioanalysis/portfolioDesign.csv
 
It looks like this:

```
XRE,0.05
XIU,0.15
XSP,0.20
XEM,0.20
XSB,0.20
XPF,0.20
```

So ETF "XRE" has a desired weight of 5% of the total portfolio value, ETF "XSP" has a desired weight of 20% of the portfolio value, etc. Note that the weights must sum to 1. You can put this file anywhere. When you select the securities, keep in mind that data for only those dates that are common to all ETFs will be included in the simulation. So if ETF XEM has data for 2002-01-30 and later but ETF XPF has data for only 2010-10-31 and later, then data for earlier than 2010-10-31 will NOT be used. Keep this in mind when choosing the investment duration (see below)    

- Select a file to which to save the results. I'll call this `path/to/results.xslx`. Make sure it ends in `.xlsx`.
- Pick an investment duration in years. Keep in mind that if you have only two years of data that are common to all the ETFs you have chosen, then choosing an investment duration of longer than two years will yield no results.
- Specify the ETF trading costs. This is the cost of purchasing (or selling) any number of shares of one ETF. I think this price is currently CAD 9.99.
- Specify the bid-ask cost as a fraction of NAV. [Hill] puts this at 0.10%. So if you buy, the price you pay is NAV*(1 + 0.0010), and if you sell, the price you get is NAV/(1 + 0.0010).
- Specify a rebalancing interval (monthly, quarterly, semiannually, or annually)

Assuming the following:

```
Investment duration (years): 5
Rebalancing interval: SemiAnnually
Initial investment: 100060
ETF Trading cost: 9.99
Bid-ask cost: 0.11%
Path to portfolio design file: /path/to/portfolioDesign.csv
Path to output file: /path/to/output.xlsx
```

Then at the command line (assuming you've already set the environment variables, set up the databases (test and prod), and sucessfully run the `ishares-etf-data-download` project), you would clone the repository, create the fat jar, and run it:                                                 
                                                 
```
cd /path/to/wherever/you/want/to/put/this/project
git clone https://github.com/shafiquejamal/portfolioanalysis.git
cd portfolioanalysis
sbt assembly
java -jar target/scala-2.11/portfolio-simulation-assembly-1.0.jar 5 SemiAnnually 100060 9.99 0.0011 0.04 /path/to/portfolioDesign.csv /path/to/output.xlsx
```                                                 

## Assumptions and Methodology

Please do give me some feedback on these:

- I assume everything is in the same currency, including the trading costs, initial investment, and the NAV, Ex-Dividend, etc. data that is in the database.
- I assume that only whole quantities of ETFs can be purchased (e.g. can't buy 0.237 shares of an ETF).
- At each rebalancing interval, I rebalance only those securities whose weight in the portfolio (excluding accumulated Ex-Dividends and cash) is different from the desired weight by the maximum allowed deviation. Rebalancing the other securities might cause the new weight after rebalancing to be different by more than the maximum allowed deviation.
- If there is enough cash left over from accumulated Ex-Dividends, previous rebalancing and transactions, I make sure to purchase more ETFs, even if this results in excessive weight deviation, because excessive deviation is less bad than having cash lying around in the account.
- To calculate annual average return, I assume that at the end the portfolio will be liquidated and so account for trading and bid-ask costs.
- I assume that ETF annual fees and commission are taken care of internally.
- The code generates many investment periods, one for each possible start date, with the investment period length equal to the investment duration. For each investment period, an annual average return is calculated.  

[Hill] Hill, Joanne M.; Nadig, Dave; Hougan, Matt (2015-05-21). A Comprehensive Guide to Exchange-Traded Funds (ETFs) (Kindle Location 1439). CFA Institute Research Foundation. Kindle Edition.  

## Sample results

Try running the simulation using the test `portfolioDesign.csv` file:

```
java -jar target/scala-2.11/portfolio-simulation-assembly-1.0.jar 5 SemiAnnually 100060 9.99 0.0011 0.04 src/test/scala/com/eigenroute/portfolioanalysis/portfolioDesign.csv /path/to/outputfile.xlsx
```

The simulation can take several minutes or more to complete. Look at the second tab in `/path/to/outputfile.xlsx`. The average of all average annual returns for all the simulation results is about 1.8%. Perhaps I need to improve the model. 
