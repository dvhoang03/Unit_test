/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package RTDRestaurant.Controller.Service;

import RTDRestaurant.Controller.Connection.DatabaseConnection;
import RTDRestaurant.Model.ModelLogin;
import RTDRestaurant.Model.ModelNguoiDung;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author ADMIN
 */
public class ServiceUserIT {

    private Connection con;
    private ServiceUser service;
    private boolean autoCommitBefore;

    @Before
    public void setUp() throws Exception {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        dbConnection.connectToDatabase();
        con = dbConnection.getConnection();

        // Lưu lại trạng thái autocommit trước khi test
        autoCommitBefore = con.getAutoCommit();

        // Tắt auto-commit để có thể rollback
        con.setAutoCommit(false);

        // Giả sử ServiceUser sử dụng Connection hiện tại
        service = new ServiceUser();
    }

    @After
    public void tearDown() throws Exception {
        if (con != null) {
            con.rollback(); // Rollback mọi thay đổi sau test
            con.setAutoCommit(autoCommitBefore); // Khôi phục trạng thái ban đầu
        }
    }

    /**
     * Test of login method, of class ServiceUser.
     */
    /**
     * Test case TC2_1: Kiểm tra đăng nhập thành công với email và mật khẩu
     * đúng. Dữ liệu: Email = "NVHoangViet@gmail.com", Mật khẩu = "123" Kỳ vọng:
     * Trả về đối tượng ModelNguoiDung chứa đúng email và mật khẩu.
     */
    @Test
    public void testLogin_TC01() throws Exception {
        ModelLogin login = new ModelLogin("NVHoangViet@gmail.com", "123");
        ModelNguoiDung result = service.login(login);
        assertNotNull("Đăng nhập thất bại - Không trả về đối tượng ModelNguoiDung", result);
        assertEquals("Email không khớp", "NVHoangViet@gmail.com", result.getEmail());
        assertEquals("Mật khẩu không khớp", "123", result.getPassword());
    }

    /**
     * Test case TC2_2: Kiểm tra đăng nhập thất bại khi mật khẩu sai. Dữ liệu:
     * Email đúng, mật khẩu sai Kỳ vọng: Trả về null.
     */
    @Test
    public void testLogin_TC02() throws Exception {
        ModelLogin login = new ModelLogin("NVHoangViet@gmail.com", "1234");
        ModelNguoiDung result = service.login(login);
        assertNull("Đăng nhập sai mật khẩu nhưng vẫn trả về đối tượng", result);
    }

    /**
     * Test case TC2_3: Kiểm tra đăng nhập thất bại khi cả email và mật khẩu đều
     * sai. Dữ liệu: Email sai, mật khẩu sai Kỳ vọng: Trả về null.
     */
    @Test
    public void testLogin_TC03() throws Exception {
        ModelLogin login = new ModelLogin("NVHoangViet1111@gmail.com", "1234");
        ModelNguoiDung result = service.login(login);
        assertNull("Đăng nhập với email và mật khẩu sai nhưng vẫn trả về đối tượng", result);
    }

    /**
     * Test case TC2_4: Kiểm tra đăng nhập thất bại khi tài khoản chưa xác minh
     * (Trangthai != 'Verified'). Dữ liệu: Email hợp lệ nhưng chưa được xác minh
     * Kỳ vọng: Trả về null.
     */
    @Test
    public void testLogin_TC04() throws Exception {
        ModelLogin login = new ModelLogin("ngan@gmail.com", "123");
        ModelNguoiDung result = service.login(login);
        assertNull("Tài khoản chưa xác minh nhưng vẫn đăng nhập thành công", result);
    }

    /**
     * Test case TC2_5: Kiểm tra đăng nhập thất bại khi không truyền email và
     * mật khẩu. Dữ liệu: Email = null, Mật khẩu = null Kỳ vọng: Trả về
     * exception.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testLogin_TC05() throws Exception {
        ModelLogin login = new ModelLogin(); // email = null, mật khẩu = null
        service.login(login);
    }

    /**
     * Test of insertUser method, of class ServiceUser.
     */
    /**
     * Test case TC01: Thêm người dùng hợp lệ với đầy đủ thông tin. Kỳ vọng: Dữ
     * liệu được thêm thành công và đúng các trường: Email, Password, Vai trò,
     * VerifyCode, ID.
     */
    @Test
    public void testInsertUser_TC01() throws Exception {
        ModelNguoiDung newUser = new ModelNguoiDung();
        newUser.setEmail("testFullCheck@gmail.com");
        newUser.setPassword("123456");

        service.insertUser(newUser);

        String sql = "SELECT * FROM NguoiDung WHERE ID_ND = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, newUser.getUserID());
        ResultSet rs = ps.executeQuery();

        assertTrue("Không tìm thấy người dùng mới", rs.next());
        assertEquals("Email không khớp", newUser.getEmail(), rs.getString("Email"));
        assertEquals("Mật khẩu không khớp", newUser.getPassword(), rs.getString("MatKhau"));
        assertEquals("Vai trò không đúng", newUser.getRole(), rs.getString("Vaitro"));
        assertEquals("VerifyCode không khớp", newUser.getVerifyCode(), rs.getString("VerifyCode"));
        assertEquals("ID người dùng không khớp", newUser.getUserID(), rs.getInt("ID_ND"));

        rs.close();
        ps.close();
    }

    /**
     * Test case TC02: Thêm người dùng với email null. Kỳ vọng: Ném SQLException
     * do vi phạm ràng buộc NOT NULL.
     */
    @Test(expected = SQLException.class)
    public void testInsertUser_TC02() throws Exception {
        ModelNguoiDung user = new ModelNguoiDung();
        user.setEmail(null);
        user.setPassword("123456");

        service.insertUser(user);
    }

    /**
     * Test case TC03: Thêm người dùng với mật khẩu null. Kỳ vọng: Ném
     * SQLException do vi phạm ràng buộc NOT NULL.
     */
    @Test(expected = SQLException.class)
    public void testInsertUser_TC03() throws Exception {
        ModelNguoiDung user = new ModelNguoiDung();
        user.setEmail("nullpass@gmail.com");
        user.setPassword(null);

        service.insertUser(user);
    }

    /**
     * Test case TC04: Thêm người dùng với email đã tồn tại. Kỳ vọng: Ném
     * SQLException do vi phạm ràng buộc UNIQUE.
     */
    @Test
    public void testInsertUser_TC04() throws Exception {
        ModelNguoiDung user1 = new ModelNguoiDung();
        user1.setEmail("duplicated@gmail.com");
        user1.setPassword("pass1");
        service.insertUser(user1);

        ModelNguoiDung user2 = new ModelNguoiDung();
        user2.setEmail("duplicated@gmail.com");
        user2.setPassword("pass2");

        try {
            service.insertUser(user2);
            fail("Kỳ vọng SQLException do email trùng nhưng không xảy ra");
        } catch (SQLException e) {
            System.out.println("Đã bắt được lỗi email trùng: " + e.getMessage());
            assertTrue(e.getMessage().toLowerCase().contains("unique") || e.getMessage().toLowerCase().contains("duplicate"));
        }
    }

    /**
     * Test case TC05: Thêm người dùng với email sai định dạng. Kỳ vọng: Ném
     * IllegalArgumentException do không hợp lệ định dạng email.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInsertUser_TC05() throws Exception {
        ModelNguoiDung user = new ModelNguoiDung();
        user.setEmail("invalidemail"); // không chứa @
        user.setPassword("abc123");

        service.insertUser(user);
    }

    /**
     * Test case TC06: Thêm người dùng với đối tượng null. Kỳ vọng: Ném
     * NullPointerException.
     */
    @Test(expected = NullPointerException.class)
    public void testInsertUser_TC06() throws Exception {
        service.insertUser(null);
    }

    /**
     * Test of testCheckDuplicateCode method, of class ServiceUser.
     */
    /**
     * Test case TC01: Mã verifyCode đã tồn tại trong bảng. Kỳ vọng: Hàm trả về
     * true.
     */
    @Test
    public void testCheckDuplicateCode_TC01() throws Exception {
        String code = "123456";

        // Thêm dữ liệu test
        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, VerifyCode, VaiTro) VALUES (?, ?, ?, ?, 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, 99999); // ID giả
        ps.setString(2, "exists@gmail.com");
        ps.setString(3, "123");
        ps.setString(4, code);
        ps.executeUpdate();
        ps.close();

        // Gọi hàm kiểm tra
        boolean result = service.checkDuplicateCode(code);
        assertTrue("Mã đã tồn tại nhưng trả về false", result);
    }

    /**
     * Test case TC02: Mã verifyCode chưa tồn tại. Kỳ vọng: Hàm trả về false.
     */
    @Test
    public void testCheckDuplicateCode_TC02() throws Exception {
        String code = "000000"; // Mã chắc chắn chưa tồn tại
        boolean result = service.checkDuplicateCode(code);
        assertFalse("Mã chưa tồn tại nhưng trả về true", result);
    }

    /**
     * Test case TC03: Truyền mã null vào checkDuplicateCode. Kỳ vọng: Ném ra
     * IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCheckDuplicateCode_TC03() throws Exception {
        service.checkDuplicateCode(null);
    }

    /**
     * Test case TC04: Truyền mã rỗng ("") vào checkDuplicateCode. Kỳ vọng: Ném
     * ra IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCheckDuplicateCode_TC04() throws Exception {
        service.checkDuplicateCode("");
    }

    /**
     * Test of checkDuplicateEmail method, of class ServiceUser.
     */
    /**
     * Test case TC01: Email đã tồn tại và trạng thái đã xác minh (Verified). Kỳ
     * vọng: Trả về true.
     */
    @Test
    public void testCheckDuplicateEmail_TC01() throws Exception {
        String email = "verified@gmail.com";
        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, Trangthai, Vaitro) VALUES (99990, ?, '123', 'Verified', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, email);
        ps.executeUpdate();
        ps.close();

        boolean result = service.checkDuplicateEmail(email);
        assertTrue("Email đã xác minh phải trả về true", result);
    }

    /**
     * Test case TC02: Email đã tồn tại nhưng chưa xác minh (Trangthai !=
     * 'Verified'). Kỳ vọng: Trả về false.
     */
    @Test
    public void testCheckDuplicateEmail_TC02() throws Exception {
        String email = "unverified@gmail.com";
        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, Trangthai, Vaitro) VALUES (99991, ?, '123', 'Pending', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, email);
        ps.executeUpdate();
        ps.close();

        boolean result = service.checkDuplicateEmail(email);
        assertFalse("Email chưa xác minh phải trả về false", result);
    }

    /**
     * Test case TC03: Email không tồn tại trong bảng NguoiDung. Kỳ vọng: Trả về
     * false.
     */
    @Test
    public void testCheckDuplicateEmail_TC03() throws Exception {
        String email = "notfound@gmail.com";
        boolean result = service.checkDuplicateEmail(email);
        assertFalse("Email chưa tồn tại phải trả về false", result);
    }

    /**
     * Test case TC04: Truyền vào null. Kỳ vọng: Ném IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCheckDuplicateEmail_TC04() throws Exception {
        service.checkDuplicateEmail(null);
    }

    /**
     * Test case TC05: Truyền vào chuỗi rỗng "". Kỳ vọng: Ném
     * IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCheckDuplicateEmail_TC05() throws Exception {
        service.checkDuplicateEmail("");
    }

    /**
     * Test of doneVerify method, of class ServiceUser.
     */
    /**
     * Test case TC01: Xác minh thành công cho người dùng chưa xác minh. Kỳ
     * vọng: Cập nhật verifyCode = '', Trangthai = 'Verified', và thêm khách
     * hàng mới.
     */
    @Test
    public void testDoneVerify_TC01() throws Exception {
        int userID = 99995;
        String email = "verifyme@gmail.com";
        String code = "999999";
        String name = "Nguyễn Văn Test";

        // Thêm người dùng giả
        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, VerifyCode, Trangthai, Vaitro) "
                + "VALUES (?, ?, ?, ?, 'Pending', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, email);
        ps.setString(3, "123");
        ps.setString(4, code);
        ps.executeUpdate();
        ps.close();

        // Gọi hàm cần test
        service.doneVerify(userID, name);

        // Kiểm tra cập nhật VerifyCode và Trangthai trong NguoiDung
        String sqlCheck = "SELECT VerifyCode, Trangthai FROM NguoiDung WHERE ID_ND = ?";
        PreparedStatement psCheck = con.prepareStatement(sqlCheck);
        psCheck.setInt(1, userID);
        ResultSet rsCheck = psCheck.executeQuery();

        assertTrue("Không tìm thấy người dùng với ID đã xác minh", rsCheck.next());
        assertEquals("VerifyCode không được xóa sau khi xác minh", "", rsCheck.getString("VerifyCode"));
        assertEquals("Trạng thái người dùng không cập nhật thành 'Verified'", "Verified", rsCheck.getString("Trangthai"));

        psCheck.close();

        // Kiểm tra khách hàng được thêm mới vào bảng KhachHang
        String sqlKH = "SELECT * FROM KhachHang WHERE ID_ND = ?";
        PreparedStatement psKH = con.prepareStatement(sqlKH);
        psKH.setInt(1, userID);
        ResultSet rsKH = psKH.executeQuery();

        assertTrue("Không tìm thấy bản ghi khách hàng tương ứng sau xác minh", rsKH.next());
        assertEquals("Tên khách hàng lưu trong bảng KhachHang không đúng", name, rsKH.getString("TenKH"));

        psKH.close();
    }

    /**
     * Test case TC02: Xác minh lại người dùng đã Verified. Kỳ vọng: Có thể cập
     * nhật lại, không lỗi, và thêm khách hàng nếu chưa có.
     */
    @Test
    public void testDoneVerify_TC02() throws Exception {
        int userID = 99996;
        String name = "Tên đã xác minh";

        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, VerifyCode, Trangthai, Vaitro) "
                + "VALUES (?, ?, ?, '', 'Verified', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, "alreadyverified@gmail.com");
        ps.setString(3, "123");
        ps.executeUpdate();
        ps.close();

        service.doneVerify(userID, name);

        String sqlKH = "SELECT * FROM KhachHang WHERE ID_ND = ?";
        PreparedStatement psKH = con.prepareStatement(sqlKH);
        psKH.setInt(1, userID);
        ResultSet rsKH = psKH.executeQuery();
        assertTrue("Không tìm thấy khách hàng", rsKH.next());
        psKH.close();
    }

    /**
     * Test case TC03: Truyền userID không tồn tại. Kỳ vọng: Ném SQLException.
     */
    @Test(expected = SQLException.class)
    public void testDoneVerify_TC03() throws Exception {
        int nonExistentUserID = 123456;
        service.doneVerify(nonExistentUserID, "Tên bất kỳ");
    }

    /**
     * Test case TC04: Truyền name = null. Kỳ vọng: Ném SQLException do lỗi ràng
     * buộc tên khách hàng.
     */
    @Test(expected = SQLException.class)
    public void testDoneVerify_TC04() throws Exception {
        int userID = 99997;
        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, VerifyCode, Trangthai, Vaitro) "
                + "VALUES (?, ?, ?, '999998', 'Pending', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, "nullname@gmail.com");
        ps.setString(3, "123");
        ps.executeUpdate();
        ps.close();

        service.doneVerify(userID, null);
    }

    /**
     * Test case TC05: Gọi doneVerify nhiều lần trên cùng userID. Kỳ vọng: Chỉ
     * thêm đúng 1 khách hàng.
     */
    @Test
    public void testDoneVerify_TC05() throws Exception {
        int userID = 99994;
        String name = "Lặp Khách Hàng";

        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, VerifyCode, Trangthai, Vaitro) "
                + "VALUES (?, ?, ?, '567890', 'Pending', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, "repeat@gmail.com");
        ps.setString(3, "123");
        ps.executeUpdate();
        ps.close();

        service.doneVerify(userID, name);
        service.doneVerify(userID, name); // gọi lại lần 2

        String sqlKH = "SELECT COUNT(*) FROM KhachHang WHERE ID_ND = ?";
        PreparedStatement psKH = con.prepareStatement(sqlKH);
        psKH.setInt(1, userID);
        ResultSet rs = psKH.executeQuery();
        rs.next();
        int count = rs.getInt(1);
        assertEquals("Chỉ được thêm 1 khách hàng", 1, count);
        psKH.close();
    }

    /**
     * Test of verifyCodeWithUser method, of class ServiceUser.
     */
    /**
     * Test case TC01: Kiểm tra mã xác minh đúng. Người dùng tồn tại và nhập
     * đúng mã xác minh. Kỳ vọng: Trả về true.
     */
    @Test
    public void testVerifyCodeWithUser_TC01() throws Exception {
        int userID = 100001;
        String code = "123456";

        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, VerifyCode, Trangthai, Vaitro) "
                + "VALUES (?, ?, ?, ?, 'Pending', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, "correctcode@gmail.com");
        ps.setString(3, "123");
        ps.setString(4, code);
        ps.executeUpdate();
        ps.close();

        boolean result = service.verifyCodeWithUser(userID, code);
        assertTrue("Mã xác minh đúng nhưng trả về false", result);
    }

    /**
     * Test case TC02: Kiểm tra mã xác minh sai. Người dùng nhập sai mã xác
     * minh. Kỳ vọng: Trả về false.
     */
    @Test
    public void testVerifyCodeWithUser_TC02() throws Exception {
        int userID = 100002;
        String realCode = "654321";
        String wrongCode = "000000";

        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, VerifyCode, Trangthai, Vaitro) "
                + "VALUES (?, ?, ?, ?, 'Pending', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, "wrongcode@gmail.com");
        ps.setString(3, "123");
        ps.setString(4, realCode);
        ps.executeUpdate();
        ps.close();

        boolean result = service.verifyCodeWithUser(userID, wrongCode);
        assertFalse("Mã sai nhưng trả về true", result);
    }

    /**
     * Test case TC03: Truyền mã xác minh null. Kỳ vọng: Ném ra
     * IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testVerifyCodeWithUser_TC03() throws Exception {
        int userID = 100003;
        String realCode = "111111";

        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, VerifyCode, Trangthai, Vaitro) "
                + "VALUES (?, ?, ?, ?, 'Pending', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, "nullcode@gmail.com");
        ps.setString(3, "123");
        ps.setString(4, realCode);
        ps.executeUpdate();
        ps.close();

        service.verifyCodeWithUser(userID, null); // ném IllegalArgumentException
    }

    /**
     * Test case TC04: Truyền mã xác minh rỗng (""). Kỳ vọng: ném
     * IllegalArgumentException
     */
    @Test(expected = IllegalArgumentException.class)
    public void testVerifyCodeWithUser_TC04() throws Exception {
        int userID = 100004;
        String realCode = "abcdef";

        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, VerifyCode, Trangthai, Vaitro) "
                + "VALUES (?, ?, ?, ?, 'Pending', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, "emptycode@gmail.com");
        ps.setString(3, "123");
        ps.setString(4, realCode);
        ps.executeUpdate();
        ps.close();

        service.verifyCodeWithUser(userID, ""); // ném IllegalArgumentException
    }

    /**
     * Test case TC05: Truyền userID không tồn tại trong bảng. Kỳ vọng: Trả về
     * false.
     */
    @Test
    public void testVerifyCodeWithUser_TC05() throws Exception {
        int userID = 999999; // ID không tồn tại
        String code = "anycode";

        boolean result = service.verifyCodeWithUser(userID, code);
        assertFalse("User không tồn tại nhưng trả về true", result);
    }

    /**
     * Test of changePassword method, of class ServiceUser.
     */
    /**
     * Test case TC01: Cập nhật mật khẩu thành công. Dữ liệu: userID hợp lệ, mật
     * khẩu mới khác mật khẩu cũ. Kết quả mong muốn: Trường MatKhau trong DB
     * được cập nhật thành mật khẩu mới.
     */
    @Test
    public void testChangePassword_TC01() throws Exception {
        int userID = 200001;
        String oldPass = "oldpass";
        String newPass = "newpass123";

        // Thêm người dùng mẫu
        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, Trangthai, VaiTro) "
                + "VALUES (?, ?, ?, 'Verified', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, "changepass@gmail.com");
        ps.setString(3, oldPass);
        ps.executeUpdate();
        ps.close();

        // Gọi hàm cập nhật mật khẩu
        service.changePassword(userID, newPass);

        // Kiểm tra kết quả
        PreparedStatement check = con.prepareStatement("SELECT MatKhau FROM NguoiDung WHERE ID_ND = ?");
        check.setInt(1, userID);
        ResultSet rs = check.executeQuery();
        assertTrue(rs.next());
        assertEquals("Mật khẩu không được cập nhật đúng", newPass, rs.getString("MatKhau"));
        check.close();
    }

    /**
     * Test case TC02: Mật khẩu mới giống mật khẩu cũ. Dữ liệu: userID hợp lệ,
     * mật khẩu mới = mật khẩu cũ. Kết quả mong muốn: Trường MatKhau vẫn giữ
     * nguyên, không bị thay đổi sai.
     */
    @Test
    public void testChangePassword_TC02() throws Exception {
        int userID = 200002;
        String password = "samepass";

        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, Trangthai, VaiTro) "
                + "VALUES (?, ?, ?, 'Verified', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, "samepass@gmail.com");
        ps.setString(3, password);
        ps.executeUpdate();
        ps.close();

        service.changePassword(userID, password);

        PreparedStatement check = con.prepareStatement("SELECT MatKhau FROM NguoiDung WHERE ID_ND = ?");
        check.setInt(1, userID);
        ResultSet rs = check.executeQuery();
        assertTrue(rs.next());
        assertEquals("Mật khẩu thay đổi không đúng", password, rs.getString("MatKhau"));
        check.close();
    }

    /**
     * Test case TC03: Mật khẩu mới là chuỗi rỗng. Mục tiêu: Đảm bảo hệ thống từ
     * chối mật khẩu rỗng bằng cách ném IllegalArgumentException. Dữ liệu:
     * userID hợp lệ, mật khẩu mới = "" Kết quả mong muốn: Ném
     * IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testChangePassword_TC03() throws Exception {
        int userID = 200003;

        // Tạo user hợp lệ
        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, Trangthai, VaiTro) "
                + "VALUES (?, ?, '123', 'Verified', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, "emptypass@gmail.com");
        ps.executeUpdate();
        ps.close();

        // Gọi với mật khẩu rỗng => phải ném IllegalArgumentException
        service.changePassword(userID, "");
    }

    /**
     * Test case TC04: Mật khẩu mới là null. Dữ liệu: newPass = null Kết quả
     * mong muốn: SQLException (do DB không chấp nhận null nếu có ràng buộc NOT
     * NULL).
     */
    @Test(expected = SQLException.class)
    public void testChangePassword_TC04() throws Exception {
        int userID = 200004;

        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, Trangthai, VaiTro) "
                + "VALUES (?, ?, '123', 'Verified', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, "nullpass@gmail.com");
        ps.executeUpdate();
        ps.close();

        service.changePassword(userID, null); // Gây lỗi
    }

    /**
     * Test case TC05: Trường hợp userID không tồn tại. Dữ liệu: userID không có
     * trong bảng. Kết quả mong muốn: Không ném lỗi, nhưng không có dòng nào bị
     * cập nhật.
     */
    @Test
    public void testChangePassword_TC05() throws Exception {
        int userID = 999999;
        service.changePassword(userID, "newpass");

        PreparedStatement check = con.prepareStatement("SELECT COUNT(*) FROM NguoiDung WHERE ID_ND = ?");
        check.setInt(1, userID);
        ResultSet rs = check.executeQuery();
        rs.next();
        assertEquals(0, rs.getInt(1)); // Không tồn tại
        check.close();
    }

    /**
     * Test case TC06: Mật khẩu vượt quá độ dài giới hạn (20 ký tự). Dữ liệu:
     * newPass > 20 ký tự. Kết quả mong muốn: SQLException với lỗi ORA-12899 do
     * vi phạm độ dài cột.
     */
    @Test(expected = SQLException.class)
    public void testChangePassword_TC06() throws Exception {
        int userID = 200006;

        String sql = "INSERT INTO NguoiDung (ID_ND, Email, MatKhau, Trangthai, VaiTro) "
                + "VALUES (?, ?, '123', 'Verified', 'Khach Hang')";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, userID);
        ps.setString(2, "toolong@gmail.com");
        ps.executeUpdate();
        ps.close();

        // Mật khẩu dài hơn 20 ký tự
        service.changePassword(userID, "123456789012345678901");
    }
}
