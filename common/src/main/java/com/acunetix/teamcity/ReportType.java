package com.acunetix.teamcity;

public enum ReportType{
	ScanDetail(3),
	OwaspTopTen2013(5),
	HIPAACompliance(6),
	PCICompliance(7),
	KnowledgeBase(8),
	ExecutiveSummary(9);
	
	private int number;
	
	ReportType(int number) {
		this.number = number;
	}
	
	public int getNumber() {
		return number;
	}
	
	public String getNumberAsString() {
		return String.valueOf(number);
	}
}
