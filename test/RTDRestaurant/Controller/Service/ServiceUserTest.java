/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package RTDRestaurant.Controller.Service;

import RTDRestaurant.Controller.Connection.DatabaseConnection;
import RTDRestaurant.Model.ModelKhachHang;
import RTDRestaurant.Model.ModelLogin;
import RTDRestaurant.Model.ModelNguoiDung;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
/**
 *
 * @author ADMIN
 */
public class ServiceUserTest {
    private Connection con;
    private ServiceUser service;
    
    @Before
    public void setUp() throws Exception {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        dbConnection.connectToDatabase();
        con = dbConnection.getConnection();
        service = new ServiceUser();
    }
    

    /**
     * Test of login method, of class ServiceUser.
     */
    @Test
    public void testLogin_TC2_1() throws Exception {
        ModelLogin login = new ModelLogin("NVHoangViet@gmail.com", "123");
        ModelNguoiDung result = service.login(login);
        System.out.println(result);
        assertEquals("NVHoangViet@gmail.com", result.getEmail());
        assertEquals("123", result.getPassword());
    }
    @Test
    public void testLogin_TC2_2() throws Exception {
        ModelLogin login = new ModelLogin("NVHoangViet@gmail.com", "1234");
        ModelNguoiDung result = service.login(login);
        assertNull(result);
    }
    @Test
    public void testLogin_TC2_3() throws Exception {
        ModelLogin login = new ModelLogin("NVHoangViet1111@gmail.com", "1234");
        ModelNguoiDung result = service.login(login);
        assertNull(result);
    }
    @Test
    public void testLogin_TC2_4() throws Exception {
        ModelLogin login = new ModelLogin("ngan@gmail.com", "123");
        ModelNguoiDung result = service.login(login);
        assertNull(result);
    }
    @Test
    public void testLogin_TC2_5() throws Exception {
        ModelLogin login = new ModelLogin();
        ModelNguoiDung result = service.login(login);
        assertNull(result);
    }

    /**
     * Test of insertUser method, of class ServiceUser.
     */
    @Test
    public void testInsertUser_TC1_41() throws Exception {
        //thêm user mới chuẩn
        //input: user{0,"hieu@gmail.com","123"}
        ModelNguoiDung user = new ModelNguoiDung();
        user.setUserID(0);
        user.setEmail("hieu@gmail.com");
        user.setPassword("123");
        
        try {
            con.setAutoCommit(false);
            service.insertUser(user);
            
            //kiểm tra
            String sql="SELECT * FROM NguoiDung WHERE Email=?";
            PreparedStatement p=con.prepareStatement(sql);
            p.setString(1, "hieu@gmail.com");
            ResultSet r= p.executeQuery();
            List<ModelNguoiDung> ds=new ArrayList<>();
            while(r.next()){
                ModelNguoiDung u = new ModelNguoiDung();
                u.setUserID(r.getInt("ID_ND"));
                u.setEmail(r.getString("Email"));
                u.setPassword(r.getString("Matkhau"));
                u.setRole(r.getString("Vaitro"));
                ds.add(u);
            }
            Assert.assertNotNull(ds);
            Assert.assertEquals(1, ds.size());
            Assert.assertEquals(user.getEmail(), ds.get(0).getEmail());
            Assert.assertEquals(user.getPassword(), ds.get(0).getPassword());
            Assert.assertEquals("Khach Hang", ds.get(0).getRole());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                con.rollback();
                con.setAutoCommit(true);
            } catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
//    @Test
//    public void testInsertUser_TC1_42() throws Exception {
//        //thêm user có email tồn tại và verified
//        //input: user{0,"hieu@gmail.com","123"}
//        ModelNguoiDung user = new ModelNguoiDung();
//        user.setUserID(0);
//        user.setEmail("hieu@gmail.com");
//        user.setPassword("123");
//        
//        ModelNguoiDung user1 = new ModelNguoiDung();
//        user1.setUserID(0);
//        user1.setEmail("hieu@gmail.com");
//        user1.setPassword("1");
//        
//        try {
//            con.setAutoCommit(false);
//            service.insertUser(user);
//            String sql="SELECT * FROM NguoiDung WHERE Email=?";
//            PreparedStatement p=con.prepareStatement(sql);
//            p.setString(1, "hieu@gmail.com");
//            ResultSet r= p.executeQuery();
//            List<ModelNguoiDung> ds=new ArrayList<>();
//            while(r.next()){
//                ModelNguoiDung u = new ModelNguoiDung();
//                u.setUserID(r.getInt("ID_ND"));
//                u.setEmail(r.getString("Email"));
//                u.setPassword(r.getString("Matkhau"));
//                u.setRole(r.getString("Vaitro"));
//                u.setVerifyCode(r.getString(3));
//                ds.add(u);
//            }
//            String code="";
//            int userID=-1;
//            if(ds.size()==1){
//                userID=ds.get(0).getUserID();
//                code=ds.get(0).getVerifyCode();
//                boolean check=service.verifyCodeWithUser(userID, code);
//                if(check){
//                    service.doneVerify(userID, "Mai Hiếu");
//                    if(service.checkDuplicateEmail(user1.getEmail())==false){
//                        service.insertUser(user1);
//                    }else{
//                        Assert.assertTrue("Email đã tồn tại ", service.checkDuplicateEmail(user1.getEmail()));
//                    }
//                }
//            }else{
//                Assert.assertTrue("Email đã tồn tại ", service.checkDuplicateEmail(user1.getEmail()));
//            }
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try{
//                con.rollback();
//                con.setAutoCommit(true);
//            } catch(Exception ex){
//                ex.printStackTrace();
//            }
//        }
//    }
//    @Test
//    public void testInsertUser_TC1_43() throws Exception {
//        System.out.println("insertUser");
//        ModelNguoiDung user = null;
//        ServiceUser instance = new ServiceUser();
//        instance.insertUser(user);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testInsertUser_TC1_44() throws Exception {
//        System.out.println("insertUser");
//        ModelNguoiDung user = null;
//        ServiceUser instance = new ServiceUser();
//        instance.insertUser(user);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testInsertUser_TC1_45() throws Exception {
//        System.out.println("insertUser");
//        ModelNguoiDung user = null;
//        ServiceUser instance = new ServiceUser();
//        instance.insertUser(user);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testInsertUser_TC1_46() throws Exception {
//        System.out.println("insertUser");
//        ModelNguoiDung user = null;
//        ServiceUser instance = new ServiceUser();
//        instance.insertUser(user);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    @Test
//    public void testInsertUser_TC1_47() throws Exception {
//        System.out.println("insertUser");
//        ModelNguoiDung user = null;
//        ServiceUser instance = new ServiceUser();
//        instance.insertUser(user);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }


    /**
     * Test of checkDuplicateEmail method, of class ServiceUser.
     */
    @Test
    public void testCheckDuplicateEmail_TC1_11() throws Exception {
        //Trường hợp email rỗng, input: "", output: false
        String email = "";
        boolean expResult = false;
        boolean result = service.checkDuplicateEmail(email);
        assertEquals(expResult, result);
    }
    @Test
    public void testCheckDuplicateEmail_TC1_12() throws Exception {
        //Trường hợp email đã tồn tại và verified, input: "nganguyetkimthi@gmail.com", output: true
        String email = "nganguyetkimthi@gmail.com";
        boolean expResult = true;
        boolean result = service.checkDuplicateEmail(email);
        assertEquals(expResult, result);
    }
    @Test
    public void testCheckDuplicateEmail_TC1_13() throws Exception {
        //Trường hợp email đã tồn tại và chưa verified, input: "ngan@gmail.com", output: false
        String email = "ngan@gmail.com";
        boolean expResult = false;
        boolean result = service.checkDuplicateEmail(email);
        assertEquals(expResult, result);
    }
    @Test
    public void testCheckDuplicateEmail_TC1_14() throws Exception {
        //Trường hợp email không tồn tại, input: "hoa@gmail.com", output: false
        String email = "hoa@gmail.com";
        boolean expResult = false;
        boolean result = service.checkDuplicateEmail(email);
        assertEquals(expResult, result);
    }
    @Test
    public void testCheckDuplicateEmail_TC1_15() throws Exception {
        //Trường hợp email không hợp lệ, input: "123", output: false
        String email = "123";
        boolean expResult = false;
        boolean result = service.checkDuplicateEmail(email);
        assertEquals(expResult, result);
    }
    /**
     * Test of doneVerify method, of class ServiceUser.
     */
    @Test
    public void kiemTraDoneVerify_TC1_71() throws SQLException {
        try {
            con.setAutoCommit(false);
            // 1. Thiết lập dữ liệu test
            int userID = 114;
            String name = "Nguyễn Văn Test";

            // 2. Thực thi phương thức cần kiểm tra
            service.doneVerify(userID, name);

            // 3. Kiểm tra kết quả trong bảng NguoiDung
            String queryND = "SELECT VerifyCode, Trangthai FROM NguoiDung WHERE ID_ND = ?";
            PreparedStatement psND = con.prepareStatement(queryND);
            psND.setInt(1, userID);
            ResultSet rsND = psND.executeQuery();

            // Kiểm tra trạng thái đã được cập nhật đúng chưa
            assertTrue("Không tìm thấy bản ghi NguoiDung", rsND.next());
            assertEquals("VerifyCode không được cập nhật đúng", null, rsND.getString("VerifyCode"));
            assertEquals("Trangthai không được cập nhật đúng", "Verified", rsND.getString("Trangthai"));
            psND.close();

            // 4. Kiểm tra kết quả trong bảng KhachHang
            String queryKH = "SELECT ID_KH, TenKH, TO_CHAR(Ngaythamgia, 'dd-MM-YYYY') as NgayTG, ID_ND FROM KhachHang WHERE ID_ND = ?";
            PreparedStatement psKH = con.prepareStatement(queryKH);
            psKH.setInt(1, userID);
            ResultSet rsKH = psKH.executeQuery();

            // Kiểm tra khách hàng mới đã được thêm đúng chưa
            assertTrue("Không tìm thấy bản ghi KhachHang", rsKH.next());
            assertNotNull("ID_KH không được tạo", rsKH.getInt("ID_KH"));
            assertEquals("TenKH không khớp", name, rsKH.getString("TenKH"));

            // Kiểm tra ngày tham gia
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-YYYY");
            String expectedDate = simpleDateFormat.format(new Date());
            assertEquals("Ngày tham gia không khớp", expectedDate, rsKH.getString("NgayTG"));

            // Kiểm tra ID_ND
            assertEquals("ID_ND không khớp", userID, rsKH.getInt("ID_ND"));
            psKH.close();
        } catch (Exception e) {
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }
    
    @Test
    public void kiemTraDoneVerifyVoiNhieuKhachHang_TC1_73() throws SQLException {
        try {
            // 1. Thêm một khách hàng đã tồn tại
            con.setAutoCommit(false);

            // 2. Thực thi phương thức với dữ liệu test
            int userID = 125;
            String name = "Nguyễn Văn Test 2";
            service.doneVerify(userID, name);

            // 3. Kiểm tra ID_KH mới phải lớn hơn ID_KH hiện tại
            String queryNewKH = "SELECT * FROM KhachHang WHERE ID_ND = ?";
            PreparedStatement psNewKH = con.prepareStatement(queryNewKH);
            psNewKH.setInt(1, userID);
            ResultSet rsNewKH = psNewKH.executeQuery();
            int i=0;
            while(rsNewKH.next()){
                i++;
            }
            assertEquals("Hàm vẫn thêm khách hàng mới cho khách hàng đã tồn tại", 1, i);
            assertTrue("Không tìm thấy khách hàng mới", rsNewKH.next());
            int newKHID = rsNewKH.getInt("ID_KH");
            psNewKH.close();

        } catch (Exception e) {
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }
    
    @Test(expected = SQLException.class)
    public void kiemTraDoneVerifyVoiLoi_TC1_72() throws SQLException {
        // 1. Cố gắng gọi phương thức với ID_ND không tồn tại
        int nonExistentUserID = 999;
        String name = "Không Tồn Tại";
        
        // 2. Thay đổi autocommit để ném ngoại lệ ngay lập tức
        con.setAutoCommit(true);
        
        // 3. Thực thi phương thức - phải ném SQLException vì không tìm thấy ID_ND
        service.doneVerify(nonExistentUserID, name);
        
        // 4. Phải fail nếu không có ngoại lệ
        fail("Kỳ vọng SQLException nhưng không có ngoại lệ nào được ném ra");
    }

    /**
     * Test of verifyCodeWithUser method, of class ServiceUser.
     */
    @Test
    public void testVerifyCodeWithUser_TC1_61() throws Exception {
        int userID =114;
        String code = "791220";
        try {
            con.setAutoCommit(false);
            ServiceUser instance = new ServiceUser();
            boolean expResult = true;
            boolean result = instance.verifyCodeWithUser(userID, code);
            assertEquals(expResult, result);
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }
    @Test
    public void testVerifyCodeWithUser_TC1_62() throws Exception {
        int userID =114;
        String code = "79122000";
        try {
            con.setAutoCommit(false);
            ServiceUser instance = new ServiceUser();
            boolean expResult = false;
            boolean result = instance.verifyCodeWithUser(userID, code);
            assertEquals(expResult, result);
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }
    @Test(expected = NullPointerException.class)
    public void testVerifyCodeWithUser_TC1_63() throws Exception {
        int userID =114;
        String code = null;
        try {
            con.setAutoCommit(false);
            ServiceUser instance = new ServiceUser();
            boolean result = instance.verifyCodeWithUser(userID, code);
            fail("Phương thức verifyCodeWithUser phải ném ra NullPointerException khi code là null");
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }
    @Test
    public void testVerifyCodeWithUser_TC1_64() throws Exception {
        int userID =114;
        String code = "abc";
        try {
            con.setAutoCommit(false);
            ServiceUser instance = new ServiceUser();
            boolean expResult = false;
            boolean result = instance.verifyCodeWithUser(userID, code);
            assertEquals(expResult, result);
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }
    /**
     * Test of changePassword method, of class ServiceUser.
     */
    @Test
    public void testChangePassword_TC4_1() throws Exception {
        int userId=125;
        String sql="SELECT * FROM NguoiDung WHERE ID_ND = ?";
        PreparedStatement p=con.prepareStatement(sql);
        p.setInt(1, 125);
        ModelNguoiDung u=new ModelNguoiDung();
        ResultSet r= p.executeQuery();
        if(r.next()){
            u.setPassword(r.getString("Matkhau"));
        }
        String newPass=u.getPassword()+"1234";
        try {
            con.setAutoCommit(false);
            service.changePassword(userId, newPass);

            //kiểm tra
            String sql1="SELECT * FROM NguoiDung WHERE ID_ND = ?";
            PreparedStatement p1=con.prepareStatement(sql1);
            p1.setInt(1, 125);
            ModelNguoiDung u1=new ModelNguoiDung();
            ResultSet r1= p1.executeQuery();
            if(r1.next()){
                u1.setPassword(r1.getString("Matkhau"));
            }
            assertNotEquals(u.getPassword(), u1.getPassword());
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
 
    }
    @Test
    public void testChangePassword_TC4_2() throws Exception {
        int userId=125;
        String sql="SELECT * FROM NguoiDung WHERE ID_ND = ?";
        PreparedStatement p=con.prepareStatement(sql);
        p.setInt(1, 125);
        ModelNguoiDung u=new ModelNguoiDung();
        ResultSet r= p.executeQuery();
        if(r.next()){
            u.setPassword(r.getString("Matkhau"));
        }
        String oldPass=u.getPassword();
        String newPass=u.getPassword();
        boolean check=true;
        try {
            con.setAutoCommit(false);
            service.changePassword(userId, newPass);
            check=false;
            assertTrue("Mật khẩu mới không được trùng với mật khẩu cũ", check);
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }
    @Test
    public void testChangePassword_TC4_3() throws Exception {
        int userId=125;
        String sql="SELECT * FROM NguoiDung WHERE ID_ND = ?";
        PreparedStatement p=con.prepareStatement(sql);
        p.setInt(1, 125);
        ModelNguoiDung u=new ModelNguoiDung();
        ResultSet r= p.executeQuery();
        if(r.next()){
            u.setPassword(r.getString("Matkhau"));
        }
        String oldPass=u.getPassword();
        String newPass="";
        try {
            con.setAutoCommit(false);
            service.changePassword(userId, newPass);
            
            //kiểm tra
            String sql1="SELECT * FROM NguoiDung WHERE ID_ND = ?";
            PreparedStatement p1=con.prepareStatement(sql1);
            p1.setInt(1, 125);
            ModelNguoiDung u1=new ModelNguoiDung();
            ResultSet r1= p1.executeQuery();
            if(r1.next()){
                u1.setPassword(r1.getString("Matkhau"));
            }
            assertEquals("mật khẩu mới không được rỗng",oldPass, u1.getPassword());
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }
    @Test
    public void testChangePassword_TC4_4() throws SQLException {
        int userId=125;
        String sql="SELECT * FROM NguoiDung WHERE ID_ND = ?";
        PreparedStatement p=con.prepareStatement(sql);
        p.setInt(1, 125);
        ModelNguoiDung u=new ModelNguoiDung();
        ResultSet r= p.executeQuery();
        if(r.next()){
            u.setPassword(r.getString("Matkhau"));
        }
        String oldPass=u.getPassword();
        String newPass="nganngan@gmail.com1234";
        try {
            con.setAutoCommit(false);
            service.changePassword(userId, newPass);
            fail("Mật khẩu không được quá dài");
        }catch(SQLException e){
            boolean check=true;
            if (e.getMessage().contains("ORA-12899")){
                check=false;
            }
            assertFalse("Hàm sai",check);
        }finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }
}
