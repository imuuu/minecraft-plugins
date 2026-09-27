package imu.iWaystone.Upgrades;

import org.bukkit.Material;

public abstract class BuildUpgrade 
{
	private Material _mat;
	private double _value;

	public BuildUpgrade(Material mat, double value) 
	{
		set_mat(mat);
		set_value(value);
		
	}

	public Material get_mat() {
		return _mat;
	}

	public void set_mat(Material _mat) {
		this._mat = _mat;
	}

	public double get_value() {
		return _value;
	}

	public void set_value(double _value) {
		this._value = _value;
	}
}
