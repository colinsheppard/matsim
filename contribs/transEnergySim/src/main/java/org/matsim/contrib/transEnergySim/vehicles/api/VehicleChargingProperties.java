package org.matsim.contrib.transEnergySim.vehicles.api;

public class VehicleChargingProperties {
	double maxLevel2ChargingPowerInKW, maxLevel3ChargingPowerInKW, maxDischargingRateInKW;
	
	public VehicleChargingProperties(double maxLevel2ChargingPowerInKW, double maxLevel3ChargingPowerInKW, double maxDischargingRateInKW) {
		this.maxLevel2ChargingPowerInKW = maxLevel2ChargingPowerInKW;
		this.maxLevel3ChargingPowerInKW = maxLevel3ChargingPowerInKW;
		this.maxDischargingRateInKW = maxDischargingRateInKW;
	}
	public double getMaxLevel2ChargingPowerInKW() {
		return maxLevel2ChargingPowerInKW;
	}

	public void setMaxLevel2ChargingPowerInKW(double maxLevel2ChargingPowerInKW) {
		this.maxLevel2ChargingPowerInKW = maxLevel2ChargingPowerInKW;
	}

	public double getMaxLevel3ChargingPowerInKW() {
		return maxLevel3ChargingPowerInKW;
	}

	public void setMaxLevel3ChargingPowerInKW(double maxLevel3ChargingPowerInKW) {
		this.maxLevel3ChargingPowerInKW = maxLevel3ChargingPowerInKW;
	}

	public double getMaxDischargingRateInKW() {
		return maxDischargingRateInKW;
	}

	public void setMaxDischargingRateInKW(double maxDischargingRateInKW) {
		this.maxDischargingRateInKW = maxDischargingRateInKW;
	}
}
