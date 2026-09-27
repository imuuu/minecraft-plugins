package imu.iAPI.Other;

//import java.text.ParseException;
import java.util.concurrent.TimeUnit;
import java.text.SimpleDateFormat;
//import java.util.Arrays;
import java.util.Date;
//import java.util.List;

public class DateParser
{
	private static SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy/HH:mm");
//	private static final List<SimpleDateFormat> DATE_FORMATS = Arrays.asList
//		   (new SimpleDateFormat("dd/MM/yyyy"),
//			new SimpleDateFormat("dd.MM.yyyy"),
//			new SimpleDateFormat("dd-MM-yyyy"));

//	public static Date ParseDate(String dateString) throws ParseException
//	{
//		for (SimpleDateFormat format : DATE_FORMATS)
//		{
//			format.setLenient(false);
//			try
//			{
//				return format.parse(dateString);
//			} catch (ParseException ignored)
//			{
//
//			}
//		}
//		throw new ParseException("Invalid date format", 0);
//	}
	
	public static Date ParseDate(String dateString) 
	{

		try
		{
			return format.parse(dateString);
		} 
		catch (Exception e)
		{
			System.out.println("Error happend parsing date, returns date now");
			return new Date();
		}
	}

	public static boolean IsDateNowOrPassed(Date date)
	{
		return date.compareTo(new Date()) <= 0;
	}
	
	public static String FormatDate(Date date) 
	{
		String formated = format.format(date);
		System.out.println("formated text: "+formated + " from: "+date);
        return formated;
    }
	
	public static String GetTimeDifference(Date date) 
	{
        long diff = date.getTime() - System.currentTimeMillis();
        long diffDays = TimeUnit.MILLISECONDS.toDays(diff);
        long diffHours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        StringBuilder sb = new StringBuilder();
        
        if (diffDays > 0) 
        {
            sb.append(diffDays + " day(s) ");
        }
        sb.append(diffHours + " hour(s) " + diffMinutes + " minute(s)");
        return sb.toString();
    }
}