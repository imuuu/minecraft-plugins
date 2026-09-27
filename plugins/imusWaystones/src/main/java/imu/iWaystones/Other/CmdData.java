package imu.iWaystones.Other;

public class CmdData
{
	String _cmd_name = "";
	String _description = "";
	String _syntax = "";
	
	public CmdData(String cmdName, String desc, String syntaxText)
	{
		_cmd_name = cmdName;
		_description = desc;
		_syntax = syntaxText;
	}

	public String get_cmd_name() {
		return _cmd_name;
	}

	public void set_cmd_name(String _cmd_name) {
		this._cmd_name = _cmd_name;
	}

	public String get_description() {
		return _description;
	}

	public void set_description(String _description) {
		this._description = _description;
	}

	public String get_syntaxText() {
		return _syntax;
	}

	public void set_syntaxText(String _syntax) {
		this._syntax = _syntax;
	}
	
}
