/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package RTDRestaurant.Controller.Service;

import RTDRestaurant.Controller.Connection.DatabaseConnection;
import RTDRestaurant.Model.ModelMessage;
import java.sql.Connection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ADMIN
 */
public class ServiceMailTest {
    private Connection con;
    private ServiceMail emailSender;
   @Before
    public void setUp() throws Exception {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        dbConnection.connectToDatabase();
        con = dbConnection.getConnection();
        emailSender = new ServiceMail();
    }

    /**
     * Test of sendMain method, of class ServiceMail.
     */
    @Test
    public void testSendMailToValidEmail() {
        // Test case 1: Gửi email đến địa chỉ hợp lệ
        // Đầu vào: 
        String validEmail = "test@gmail.com"; // Sử dụng email thật mà bạn có thể kiểm tra
        String verifyCode = "123456";
        
        // Gọi phương thức cần test
        ModelMessage result = emailSender.sendMain(validEmail, verifyCode);
        
        // Output kỳ vọng:
        // - success = true (email gửi thành công)
        // - message = "" (không có thông báo lỗi)
        assertTrue("Email hợp lệ phải gửi thành công", result.isSuccess());
        
    }
    
    @Test
    public void testSendMailToInvalidEmailFormat() {
        // Test case 2: Gửi email đến địa chỉ có định dạng không hợp lệ
        // Đầu vào:
        String invalidEmail = "invalid.email@"; // Định dạng email không hợp lệ
        String verifyCode = "123456";
        
        // Gọi phương thức cần test
        ModelMessage result = emailSender.sendMain(invalidEmail, verifyCode);
        
        // Output kỳ vọng:
        // - success = false (email gửi thất bại)
        // - message = "Email không chính xác" (thông báo lỗi cụ thể)
        assertFalse("Email không hợp lệ phải gửi thất bại", result.isSuccess());
        assertEquals("Email không hợp lệ", result.getMessage());
    }
    
    @Test
    public void testSendMailToNonExistentDomain() {
        // Test case 3: Gửi email đến domain không tồn tại
        // Đầu vào:
        String nonExistentDomainEmail = "test@nonexistentdomain123456789.com";
        String verifyCode = "123456";
        
        // Gọi phương thức cần test
        ModelMessage result = emailSender.sendMain(nonExistentDomainEmail, verifyCode);
        
        // Output kỳ vọng:
        // - success = false (email gửi thất bại)
        // - message = "Lỗi" (thông báo lỗi chung)
        assertFalse("Email với domain không tồn tại phải gửi thất bại", result.isSuccess());
        assertEquals("Thông báo lỗi phải là 'Email không tồn tại'", "Email không tồn tại", result.getMessage());
    }
    
    @Test
    public void testSendMailWithEmptyEmail() {
        // Test case 4: Gửi email với địa chỉ rỗng
        // Đầu vào:
        String emptyEmail = "";
        String verifyCode = "123456";
        
        // Gọi phương thức cần test
        ModelMessage result = emailSender.sendMain(emptyEmail, verifyCode);
        
        // Output kỳ vọng:
        // - success = false (email gửi thất bại)
        // - message = "Email không chính xác" (thông báo lỗi cụ thể)
        assertFalse("Email rỗng phải gửi thất bại", result.isSuccess());
        assertEquals("Thông báo lỗi phải là 'Email rỗng'", "Email rỗng", result.getMessage());
    }
    
    @Test(expected = NullPointerException.class)
    public void testSendMailWithNullEmail() {
        // Test case 5: Gửi email với địa chỉ null
        // Đầu vào:
        String nullEmail = null;
        String verifyCode = "123456";
        
        // Gọi phương thức cần test
        ModelMessage result = emailSender.sendMain(nullEmail, verifyCode);
        assertFalse("Email null phải trả về thất bại", result.isSuccess());
    }
}
