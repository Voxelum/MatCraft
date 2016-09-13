package rozo.alex.mathcraft.item;

/**
 * @author ci010
 */
public class ExpressionHelper
{
	public static int[] findNumberOfParameters(String inps)
	{
		int[] nofP = new int[3];
		int braketCounter = 0;
		for (int i = 0; i < inps.length(); i++)
		{
			if (inps.charAt(i) == '(')
			{
				braketCounter++;
			}
			else if (inps.charAt(i) == ')')
			{
				braketCounter--;
			}
			if (braketCounter < 0)
			{
				return null;
			}
			switch (inps.charAt(i))
			{
				case 'x': case 'X': nofP[0]++;
				break;
				case 'Z': case 'z': nofP[1]++;
				break;
				case 't': case 'T': nofP[2]++;
			}

		}

		if (nofP[2] != 0 && (nofP[0] != 0 || nofP[1] != 0))
		{
			return null;
		}

		if (braketCounter == 0)
		{
			return nofP;
		}
		else
		{
			return null;
		}
	}


	public static Double toBountry(String inp, boolean isOn)
	{
		String temp = "";
		if (isOn)
		{
			for (int i = 0; i < inp.length(); i++)
			{
				if (inp.charAt(i) == ',')
				{
					isOn = false;
				}
				if (isOn && inp.charAt(i) != ' ')
				{
					temp = temp + String.valueOf(inp.charAt(i));
				}
			}
		}
		else
		{
			for (int i = 0; i < inp.length(); i++)
			{
				if (isOn && inp.charAt(i) != ' ')
				{
					temp = temp + String.valueOf(inp.charAt(i));
				}
				if (inp.charAt(i) == ',')
				{
					isOn = true;
				}

			}
		}
		if (isNumeric(temp))
		{
			return Double.parseDouble(temp);
		}
		else
		{
			System.out.println("null situation: " + temp);
			return null;
		}
	}

	public static boolean isNumeric(String str)
	{
		try
		{
			double d = Double.parseDouble(str);
		}
		catch (NumberFormatException nfe)
		{
			return false;
		}
		return true;
	}


	public static boolean isEmpty(String[] inps)
	{
		String s = "";
		for (int i = 0; i < 4; ++i)
		{
			s = s + inps[i];
		}
		return (s.replaceAll(" ", "").equals(""));
	}

}
