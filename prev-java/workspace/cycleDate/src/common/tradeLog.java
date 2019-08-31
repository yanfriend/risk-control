package common;

class tradeLog {
	public float finalResult; 
	
	public int allTrades; 
	public int allWinningTrades;
	public int allLosingTrades; 
	
	public float winningPoints, losingPoints; 
		
	public int consecutiveWin, consecutiveLoss; 
	
	
	//  -------------------------------
	private float avgPointsPerWin, avgPointsPerLose; 
	
	private float maxintradayDrawdown; 

	private float avgPointsPerTrade; 
	private float winningPer; 
	private float losingPer;
	
	private float winlossRatio; 
	private float profitFactor;
	
	public float getMaxintradayDrawdown() {
		return maxintradayDrawdown;
	}
	public void setMaxintradayDrawdown(float maxintradayDrawdown) {
		// assert maxintradayDrawdown<=0; 
		if (this.maxintradayDrawdown > maxintradayDrawdown) this.maxintradayDrawdown = maxintradayDrawdown;
	}
	
	public float getAvgPointsPerTrade() {
		return (allTrades==0)?0:finalResult/allTrades;
	}
//	public void setAvgWinPerTrade(float avgWinPerTrade) {
//		this.avgWinPerTrade = avgWinPerTrade;
//	}
	public float getWinningPer() {
		return (allTrades==0)?0:((float)allWinningTrades)/allTrades*100;
	}
//	public void setWinningPer(float winningPer) {
//		this.winningPer = winningPer;
//	}
	public float getLosingPer() {
		return (allTrades==0)?0:((float)allLosingTrades)/allTrades*100;
	}
//	public void setLosingPer(float losingPer) {
//		this.losingPer = losingPer;
//	}
	public float getWinlossRatio() {
		return (allLosingTrades==0)?0:(float)allWinningTrades/allLosingTrades;
	}
//	public void setWinlossRatio(float winlossRatio) {
//		this.winlossRatio = winlossRatio;
//	}
	public float getProfitFactor() {
		if (allWinningTrades == 0) return 0;
		if (losingPoints==0) return Float.MAX_VALUE;
		if (allLosingTrades==0) return Float.MAX_VALUE;
		return -(winningPoints/allWinningTrades)/(losingPoints/allLosingTrades);
	}
//	public void setProfitFactor(float profitFactor) {
//		this.profitFactor = profitFactor;
//	} 
	
	public float getAvgPointsPerLose() {
		return (allLosingTrades==0)?0:losingPoints/allLosingTrades;
	}
//	public void setAvgPointsPerLose(float avgPointsPerLose) {
//		this.avgPointsPerLose = avgPointsPerLose;
//	}
	public float getAvgPointsPerWin() {
		return (allWinningTrades==0)?0:winningPoints/allWinningTrades;
	}
//	public void setAvgPointsPerWin(float avgPointsPerWin) {
//		this.avgPointsPerWin = avgPointsPerWin;
//	}
	
	static public String printTitle() {
		return String.format("allNetResult, totalTrades, profitablePer, winningTrades, losingPer, losingTrades, avgPointsPerWin, avgPointsPerLose, win/lossRatio, pointPerTrade"
				+ "consecutiveWin, consecutiveLoss, maxDrawdown, profitFactor"); 
	}
	
	public String toString() {
		return String.format("%f, %d, %f%%, %d, "+
	               "%f%%, %d, %f, %f, "+
				    "%f, %f, %d, %d, %f, %f", 
				this.finalResult, this.allTrades, this.getWinningPer(), this.allWinningTrades, 
				this.getLosingPer(), this.allLosingTrades, this.getAvgPointsPerWin(), this.getAvgPointsPerLose(),
				this.getWinlossRatio(), this.getAvgPointsPerTrade(), this.consecutiveWin, this.consecutiveLoss, this.getMaxintradayDrawdown(), this.getProfitFactor()); 
	}
}