package imu.GS.Other;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import imu.iAPI.Other.Metods;

public class MaterialOverflow 
{
	private Material _mat;
	private int _softCap;
	private double _dropProsent;
	private int _batchSize;
	private double _minPrice;
	private boolean _checkMaterialCount = false;
	public MaterialOverflow(Material _mat, int _softCap, double _dropProsent, int _batchSize, double minPrice) {

		this._mat = _mat;
		this._softCap = _softCap;
		this._dropProsent = _dropProsent;
		this._batchSize = _batchSize;
		this._minPrice = minPrice;

		if(Metods._ins.hasDurability(new ItemStack(_mat))) _checkMaterialCount = true;
	}
	public Material get_mat() {
		return _mat;
	}
	public void set_mat(Material _mat) {
		this._mat = _mat;
	}
	public int get_softCap() {
		return _softCap;
	}
	public void set_softCap(int _softCap) {
		this._softCap = _softCap;
	}
	public double get_dropProsent() {
		return _dropProsent;
	}
	public void set_dropProsent(double _dropProsent) {
		this._dropProsent = _dropProsent;
	}
	public int Get_batchSize() {
		return _batchSize;
	}
	public void Set_batchSize(int _capTrigger) {
		this._batchSize = _capTrigger;
	}
	public double Get_minPrice() {
		return _minPrice;
	}
	public void Set_minPrice(double _minPrice) {
		this._minPrice = _minPrice;
	}
	public boolean Is_checkMaterialCount() {
		return _checkMaterialCount;
	}
//	public void Set_checkMaterialCount(boolean _checkMaterialCount) {
//		this._checkMaterialCount = _checkMaterialCount;
//	}
	
	
}
