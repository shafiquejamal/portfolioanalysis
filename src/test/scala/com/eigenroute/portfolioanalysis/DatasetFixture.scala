package com.eigenroute.portfolioanalysis

import com.eigenroute.portfolioanalysis.rebalancing.{ETFDataPlus, FinalPortfolioQuantityToHave}
import com.eigenroute.portfolioanalysis.util.RichJoda._
import org.apache.spark.sql.{Dataset, SparkSession}
import org.joda.time.DateTime

object DatasetFixture extends PortfolioFixture {

  implicit val spark = SparkSession.builder().appName("financial_data").master("local").getOrCreate()
  import spark.implicits._

  val investmentFixture = new InvestmentFixture {}
  import investmentFixture._

  val sortedCommonDatesDataset = sortedCommonDatesETFData.toDS().persist()

  val sortedCommonDatesLessDatesToOmitPlusNonCommon = (sortedCommonDatesETFData ++ Seq(
      ETFDataPlus(new DateTime(commonStartDate.minusDays(1)), eTFA, "1", 20d, 0d, 0d, 0),
      ETFDataPlus(new DateTime(commonStartDate.minusDays(2)), eTFB, "1", 30d, 0d, 0d, 0),
      ETFDataPlus(new DateTime(commonStartDate.minusDays(3)), eTFC, "1", 40d, 0d, 0d, 0),
      ETFDataPlus(new DateTime(commonStartDate.minusDays(4)), eTFD, "1", 50d, 0d, 0d, 0)
                                                                                      ) ++ Seq(
      ETFDataPlus(new DateTime(commonStartDate.plusDays(maxDaysToAdd + 1)), eTFA, "1", 0d, 0d, 0d, 0),
      ETFDataPlus(new DateTime(commonStartDate.plusDays(maxDaysToAdd + 2)), eTFB, "1", 0d, 0d, 0d, 0),
      ETFDataPlus(new DateTime(commonStartDate.plusDays(maxDaysToAdd + 3)), eTFC, "1", 0d, 0d, 0d, 0),
      ETFDataPlus(new DateTime(commonStartDate.plusDays(maxDaysToAdd + 4)), eTFD, "1", 0d, 0d, 0d, 0)
    )).filterNot{ eTFData => datesToOmit.contains(new DateTime(eTFData.asOfDate.getTime))}.sortBy(_.asOfDate.getTime)
  .toDS().persist()

  val investmentInputDatasetQuarterly: Dataset[ETFDataPlus] = investmentInputDataQuarterly.toDS

  val expectedQuantitiesQuarterly: Map[Int, Seq[FinalPortfolioQuantityToHave]] =
    Map(
      1 -> Seq(
        FinalPortfolioQuantityToHave(eTFA, 125),
        FinalPortfolioQuantityToHave(eTFB, 166),
        FinalPortfolioQuantityToHave(eTFC, 25),
        FinalPortfolioQuantityToHave(eTFD, 30)
      ),
      2 -> Seq(
        FinalPortfolioQuantityToHave(eTFA, 88),
        FinalPortfolioQuantityToHave(eTFB, 212),
        FinalPortfolioQuantityToHave(eTFC, 17),
        FinalPortfolioQuantityToHave(eTFD, 41)
      ),
      3 -> Seq(
        FinalPortfolioQuantityToHave(eTFA, 388),
        FinalPortfolioQuantityToHave(eTFB, 152),
        FinalPortfolioQuantityToHave(eTFC, 19),
        FinalPortfolioQuantityToHave(eTFD, 38)
      ),
      4 -> Seq(
        FinalPortfolioQuantityToHave(eTFA, 184),
        FinalPortfolioQuantityToHave(eTFB, 246),
        FinalPortfolioQuantityToHave(eTFC, 41),
        FinalPortfolioQuantityToHave(eTFD, 47)
      )
    )
  val expectedRebalancedPortfolioQuarterly: Dataset[ETFDataPlus] = investmentInputDatasetQuarterly.map { eTFData =>
    if (eTFData.asOfDate isBefore startDatePlus3months)
      eTFData.copy(
        quantity = BigDecimal(expectedQuantitiesQuarterly(1).find(_.eTFCode == eTFData.eTFCode).map(_.quantity).getOrElse(0)),
        cash = 9.0220
      )
    else if (eTFData.asOfDate isBefore startDatePlus6months)
      eTFData.copy(
        quantity = BigDecimal(expectedQuantitiesQuarterly(2).find(_.eTFCode == eTFData.eTFCode).map(_.quantity).getOrElse(0)),
        cash = 1.5259217860353611027869343721905
      )
    else if (eTFData.asOfDate isBefore startDatePlus9months)
      eTFData.copy(
        quantity = BigDecimal(expectedQuantitiesQuarterly(3).find(_.eTFCode == eTFData.eTFCode).map(_.quantity).getOrElse(0)),
        cash = 4.5557653581060833083608031165721
      )
    else
      eTFData.copy(
        quantity = BigDecimal(expectedQuantitiesQuarterly(4).find(_.eTFCode == eTFData.eTFCode).map(_.quantity).getOrElse(0)),
        cash = 1.1541624213365298172010788133061
      )
  }

  val investmentInputDatasetSemiAnnually = investmentInputDataSemiAnnually.toDS

  val expectedQuantitiesSemiAnnually: Map[Int, Seq[FinalPortfolioQuantityToHave]] =
    Map(
      1 -> Seq(
        FinalPortfolioQuantityToHave(eTFA, 125),
        FinalPortfolioQuantityToHave(eTFB, 166),
        FinalPortfolioQuantityToHave(eTFC, 25),
        FinalPortfolioQuantityToHave(eTFD, 30)
      ),
      2 -> Seq(
        FinalPortfolioQuantityToHave(eTFA, 320),
        FinalPortfolioQuantityToHave(eTFB, 128),
        FinalPortfolioQuantityToHave(eTFC, 25),
        FinalPortfolioQuantityToHave(eTFD, 30)
      ),
      3 -> Seq(
        FinalPortfolioQuantityToHave(eTFA, 154),
        FinalPortfolioQuantityToHave(eTFB, 217),
        FinalPortfolioQuantityToHave(eTFC, 25),
        FinalPortfolioQuantityToHave(eTFD, 50)
      ),
      4 -> Seq(
        FinalPortfolioQuantityToHave(eTFA, 154),
        FinalPortfolioQuantityToHave(eTFB, 217),
        FinalPortfolioQuantityToHave(eTFC, 25),
        FinalPortfolioQuantityToHave(eTFD, 50)
      ),
      5 -> Seq(
        FinalPortfolioQuantityToHave(eTFA, 94),
        FinalPortfolioQuantityToHave(eTFB, 217),
        FinalPortfolioQuantityToHave(eTFC, 64),
        FinalPortfolioQuantityToHave(eTFD, 141)
      ),
      6 -> Seq(
        FinalPortfolioQuantityToHave(eTFA, 94),
        FinalPortfolioQuantityToHave(eTFB, 301),
        FinalPortfolioQuantityToHave(eTFC, 272),
        FinalPortfolioQuantityToHave(eTFD, 57)
      )
    )

  val expectedRebalancedPortfolioSemiAnnually: Dataset[ETFDataPlus] = investmentInputDatasetSemiAnnually.map { eTFData =>
    if (eTFData.asOfDate isBefore startDatePlus6months)
      eTFData.copy(
        quantity =
          BigDecimal(expectedQuantitiesSemiAnnually(1).find(_.eTFCode == eTFData.eTFCode).map(_.quantity).getOrElse(0)),
        cash = 9.0220
      )
    else if (eTFData.asOfDate isBefore startDatePlus12months)
      eTFData.copy(
        quantity =
          BigDecimal(expectedQuantitiesSemiAnnually(2).find(_.eTFCode == eTFData.eTFCode).map(_.quantity).getOrElse(0)),
        cash = 0.78929647387873339326740585356100
      )
    else if (eTFData.asOfDate isBefore startDatePlus18months)
      eTFData.copy(
        quantity =
          BigDecimal(expectedQuantitiesSemiAnnually(3).find(_.eTFCode == eTFData.eTFCode).map(_.quantity).getOrElse(0)),
        cash = 38.08031884926580761162721006892400
      )
    else if (eTFData.asOfDate isBefore startDatePlus24months)
      eTFData.copy(
        quantity =
          BigDecimal(expectedQuantitiesSemiAnnually(4).find(_.eTFCode == eTFData.eTFCode).map(_.quantity).getOrElse(0)),
        cash = 38.08031884926580761162721006892400
      )
    else if (eTFData.asOfDate isBefore startDatePlus30months)
      eTFData.copy(
        quantity =
          BigDecimal(expectedQuantitiesSemiAnnually(5).find(_.eTFCode == eTFData.eTFCode).map(_.quantity).getOrElse(0)),
        cash = 18.32419688342822894815702727000400
      )
    else
      eTFData.copy(
        quantity =
          BigDecimal(expectedQuantitiesSemiAnnually(6).find(_.eTFCode == eTFData.eTFCode).map(_.quantity).getOrElse(0)),
        cash = 2.51041084806712616122265507941400
      )
    }
}
