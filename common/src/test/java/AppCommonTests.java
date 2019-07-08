import com.acunetix.teamcity.AppCommon;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;

public class AppCommonTests{
	@Test
	public void LocalURLValidation() {
		AppCommon appCommon = new AppCommon();
		boolean isValid = appCommon.IsUrlValid("http://localhost:2097");
		Assert.assertTrue(isValid);
	}
	
	@Test
	public void OnpremisesServerURLValidation() {
		AppCommon appCommon = new AppCommon();
		boolean isValid = appCommon.IsUrlValid("http://TFSServer:2097");
		Assert.assertTrue(isValid);
	}
	
	@Test
	public void IsGuIDValid() {
		String guidString = "eb3eecdc266d4b801230a7ee03737f48";
		AppCommon appCommon = new AppCommon();
		boolean actual = appCommon.IsGUIDValid(guidString);
		
		Assert.assertTrue(actual);
	}
	
	@Test
	public void IsBaseURLValid() throws MalformedURLException {
		String actual = AppCommon.GetBaseURL("http://localhost:2097/").toString();
		
		Assert.assertEquals("http://localhost:2097/",actual);
	}
}
