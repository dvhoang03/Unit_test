package RTDRestaurant.Controller.Service;

import java.sql.ResultSet;
import RTDRestaurant.Controller.Connection.DatabaseConnection;
import RTDRestaurant.Model.ModelBan;
import RTDRestaurant.Model.ModelCTNK;
import RTDRestaurant.Model.ModelCTXK;
import RTDRestaurant.Model.ModelHoaDon;
import RTDRestaurant.Model.ModelKhachHang;
import RTDRestaurant.Model.ModelKho;
import RTDRestaurant.Model.ModelNguyenLieu;
import RTDRestaurant.Model.ModelNhanVien;
import RTDRestaurant.Model.ModelPNK;
import RTDRestaurant.Model.ModelPXK;
import java.sql.*;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import java.lang.reflect.Field;

public class ServiceStaffTest {

    ServiceStaff serviceStaff;
    private Connection con;

    public ServiceStaffTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        dbConnection.connectToDatabase();
        con = dbConnection.getConnection();
        serviceStaff = new ServiceStaff();
    }

    @After
    public void tearDown() throws Exception {
    }

    //lấy thông tin nhân viên có userId tồn tại
    @Test
    public void testGetStaff_S1() throws Exception {
        int userID = 102;
        ServiceStaff instance = new ServiceStaff();
        ModelNhanVien result = instance.getStaff(userID);

        ModelNhanVien expResult = null;
        String sql = "SELECT ID_NV, TenNV, to_char(NgayVL, 'dd-mm-yyyy') AS Ngay, SDT, Chucvu, ID_NQL FROM NhanVien WHERE ID_ND=?";
        PreparedStatement p = con.prepareStatement(sql);
        p.setInt(1, userID);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id_NV = r.getInt("ID_NV");
            String tenNV = r.getString("TenNV");
            String ngayVL = r.getString("Ngay");
            String sdt = r.getString("SDT");
            String chucvu = r.getString("Chucvu");
            int id_NQL = r.getInt("ID_NQL");
            expResult = new ModelNhanVien(id_NV, tenNV, ngayVL, sdt, chucvu, id_NQL);
        }
        assertNotNull("Kết quả trả về từ getStaff() là null", result);
        assertEquals("Sai ID_NV", expResult.getId_NV(), result.getId_NV());
        assertEquals("Sai TenNV", expResult.getTenNV(), result.getTenNV());
        assertEquals("Sai NgayVL", expResult.getNgayVL(), result.getNgayVL());
        assertEquals("Sai SDT", expResult.getSdt(), result.getSdt());
        assertEquals("Sai Chucvu", expResult.getChucvu(), result.getChucvu());
        assertEquals("Sai ID_NQL", expResult.getId_NQL(), result.getId_NQL());

    }

    //lấy thông tin nhân viên có userId không tồn tại
    @Test
    public void testGetStaff_S2() throws Exception {
        int userID = 9999;
        ServiceStaff instance = new ServiceStaff();
        Exception exception = assertThrows(Exception.class, () -> {
            instance.getStaff(userID);
        });
        assertEquals("không tìm thấy nhân viên", exception.getMessage());
    }

    //đổi tên thành công cho nhân viên tồn tại trong hệ thống
    @Test
    public void testReNameStaff_S3() throws Exception {
        try {
            con.setAutoCommit(false);
            ServiceStaff service = new ServiceStaff();
            ModelNhanVien data = service.getStaff(102);
            String oldName = data.getTenNV();
            String newName = "mmmmmmm";
            data.setTenNV(newName);
            service.reNameStaff(data);

            ModelNhanVien data1 = service.getStaff(102);
            assertEquals("Tên không đổi", data.getTenNV(), data1.getTenNV());
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    //đổi tên thất bại cho nhân viên không tồn tại trong hệ thống
    @Test
    public void testReNameStaff_S4() throws SQLException {
        con.setAutoCommit(false);
        ServiceStaff service = new ServiceStaff();
        ModelNhanVien data = new ModelNhanVien();
        data.setId_NV(99999);
        data.setTenNV("Nguyễn Văn Mười");
        Exception ex = assertThrows(IllegalArgumentException.class, () -> service.reNameStaff(data));
        assertEquals("Không tìm thấy nhân viên", ex.getMessage());
        con.rollback();
        con.setAutoCommit(true);
    }

    //đổi tên thất bại do tên để trống
    @Test
    public void testReNameStaff_S5() throws SQLException {
        try {
            con.setAutoCommit(false);
            ServiceStaff service = new ServiceStaff();
            ModelNhanVien data = service.getStaff(102);
            String oldName = data.getTenNV();
            String newName = "Nguyễn Văn B";
            data.setTenNV(newName);
            service.reNameStaff(data);

            Exception ex = assertThrows(IllegalArgumentException.class, () -> service.reNameStaff(data));
            assertEquals("Tên không được để trống", ex.getMessage());
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    //đổi tên thất bại do tên chứa số và ký tự đặc biệt
    @Test
    public void testReNameStaff_S6() throws SQLException {
        try {
            con.setAutoCommit(false);
            ServiceStaff service = new ServiceStaff();
            ModelNhanVien data = service.getStaff(102);
            String oldName = data.getTenNV();
            String newName = "Nguyễn Văn 123!!!";
            data.setTenNV(newName);
            service.reNameStaff(data);

            Exception ex = assertThrows(IllegalArgumentException.class, () -> service.reNameStaff(data));
            assertEquals("Tên không được chứa ký tự đặc biệt", ex.getMessage());
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /*   
     * Test case kiểm tra hàm MenuNL() Trường hợp: Bảng NguyenLieu có dữ liệu
     * Mục đích: Kiểm tra lấy đúng dữ liệu nguyên liệu đã insert
     */
    @Test
    public void testMenuNL_S7() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        stm.execute("INSERT INTO NguyenLieu VALUES (200, 'Thịt gà', 40000, 'kg')");
        stm.execute("INSERT INTO NguyenLieu VALUES (201, 'Sữa tươi', 30000, 'l')");

        ArrayList<ModelNguyenLieu> list = serviceStaff.MenuNL();

        assertTrue(list.stream().anyMatch(x -> x.getTenNL().equals("Thịt gà")));
        assertTrue(list.stream().anyMatch(x -> x.getTenNL().equals("Sữa tươi")));

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test case kiểm tra hàm MenuNL() Trường hợp: Insert dữ liệu có đơn giá = 0
     * Mục đích: Kiểm tra KHÔNG lấy dữ liệu có đơn giá = 0
     */
    @Test
    public void testMenuNL_S8() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        stm.execute("INSERT INTO NguyenLieu VALUES (204, 'Ớt', 0, 'kg')");

        ArrayList<ModelNguyenLieu> list = serviceStaff.MenuNL();

        // Không được chứa đơn giá = 0
        assertTrue("Fail: Vẫn còn nguyên liệu có đơn giá = 0 trong danh sách.",
                list.stream().noneMatch(x -> x.getDonGia() == 0));

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test case kiểm tra hàm MenuNL() Trường hợp: Insert dữ liệu có đơn giá âm
     * Mục đích: Kiểm tra KHÔNG lấy dữ liệu có đơn giá âm
     */
    @Test
    public void testMenuNL_S9() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        stm.execute("INSERT INTO NguyenLieu VALUES (205, 'Hành tây', -5000, 'kg')");

        ArrayList<ModelNguyenLieu> list = serviceStaff.MenuNL();

        // Không được chứa đơn giá âm
        assertTrue("Fail: Vẫn còn nguyên liệu có đơn giá âm trong danh sách.",
                list.stream().noneMatch(x -> x.getDonGia() < 0));

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test case kiểm tra hàm MenuNL() Trường hợp: Bảng NguyenLieu không có dữ
     * liệu test thêm Mục đích: Kiểm tra lấy dữ liệu, không có ID >= 200
     */
    @Test
    public void testMenuNL_S10() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        stm.execute("DELETE FROM NguyenLieu WHERE ID_NL >= 200");

        ArrayList<ModelNguyenLieu> list = serviceStaff.MenuNL();

        assertTrue(list.stream().noneMatch(x -> x.getId() >= 200));

        con.rollback();
        con.setAutoCommit(true);
    }

    //------------------------------------------------------------
// Test getNLbyID() - Lấy thông tin nguyên liệu theo ID
//------------------------------------------------------------
    /**
     * Test case kiểm tra hàm getNLbyID() Trường hợp: ID nguyên liệu tồn tại Mục
     * đích: Kiểm tra lấy đúng dữ liệu theo ID
     */
    @Test
    public void testGetNLbyID_S11() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        stm.execute("INSERT INTO NguyenLieu VALUES (300, 'Bột ngọt', 15000, 'kg')");

        ModelNguyenLieu nl = serviceStaff.getNLbyID(300);

        assertNotNull("Phải lấy được nguyên liệu", nl);
        assertEquals(300, nl.getId());
        assertEquals("Bột ngọt", nl.getTenNL());
        assertEquals(15000, nl.getDonGia());
        assertEquals("kg", nl.getDvt());

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test case kiểm tra hàm getNLbyID() Trường hợp: ID nguyên liệu không tồn
     * tại Mục đích: Kiểm tra trả về null khi không có dữ liệu
     */
    @Test
    public void testGetNLbyID_S12() throws Exception {
        con.setAutoCommit(false);

        ModelNguyenLieu nl = serviceStaff.getNLbyID(9999);

        assertNull("Phải trả về null nếu không tồn tại", nl);

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test case kiểm tra hàm getNLbyID() Trường hợp: ID = 0 Mục đích: Kiểm tra
     * biên ID = 0
     */
    @Test
    public void testGetNLbyID_S13() throws Exception {
        con.setAutoCommit(false);

        ModelNguyenLieu nl = serviceStaff.getNLbyID(0);

        assertNull("Phải trả về null nếu ID = 0", nl);

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test case kiểm tra hàm getNLbyID() Trường hợp: ID âm Mục đích: Kiểm tra
     * biên ID âm
     */
    @Test
    public void testGetNLbyID_S14() throws Exception {
        con.setAutoCommit(false);

        ModelNguyenLieu nl = serviceStaff.getNLbyID(-10);

        assertNull("Phải trả về null nếu ID âm", nl);

        con.rollback();
        con.setAutoCommit(true);
    }

    //------------------------------------------------------------
// Test getNextID_NL() - Lấy ID nguyên liệu tiếp theo
//------------------------------------------------------------
    /**
     * Test case kiểm tra hàm getNextID_NL() Trường hợp: Bảng NguyenLieu có dữ
     * liệu Mục đích: Kiểm tra lấy ID tiếp theo = Max(ID_NL) + 1
     */
    @Test
    public void testGetNextID_NL_S15() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        stm.execute("INSERT INTO NguyenLieu VALUES (400, 'Muối', 10000, 'kg')");
        stm.execute("INSERT INTO NguyenLieu VALUES (401, 'Đường', 15000, 'kg')");

        int nextID = serviceStaff.getNextID_NL();

        assertEquals(402, nextID);

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test case kiểm tra hàm getNextID_NL() Trường hợp: ID lớn Mục đích: Kiểm
     * tra hoạt động đúng khi ID rất lớn
     */
    @Test
    public void testGetNextID_NL_S16() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        stm.execute("INSERT INTO NguyenLieu VALUES (999999, 'Mật ong', 20000, 'l')");

        int nextID = serviceStaff.getNextID_NL();

        assertEquals(1000000, nextID);

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test case kiểm tra hàm getNextID_NL() Trường hợp: Lỗi SQL (bị mất bảng
     * NguyenLieu) Mục đích: Phải ném SQLException
     */
    @Test(expected = SQLException.class)
    public void testGetNextID_NL_S17() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        stm.execute("DROP TABLE NguyenLieu");

        serviceStaff.getNextID_NL();

        con.rollback();
        con.setAutoCommit(true);
    }

    //------------------------------------------------------------
// Test InsertNL() - Thêm mới nguyên liệu
//------------------------------------------------------------
    /**
     * Test: Insert dữ liệu hợp lệ
     */
    @Test
    public void testInsertNL_S18() throws Exception {
        con.setAutoCommit(false);

        ModelNguyenLieu nl = new ModelNguyenLieu(500, "Me", 20000, "kg");

        serviceStaff.InsertNL(nl);

        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery("SELECT * FROM NguyenLieu WHERE ID_NL = 500");

        assertTrue("Fail: Không tìm thấy dữ liệu vừa insert.", rs.next());
        assertEquals("Fail: Tên NL không đúng.", "Me", rs.getString("TenNL"));
        assertEquals("Fail: Đơn giá không đúng.", 20000, rs.getInt("DonGia"));
        assertEquals("Fail: Đơn vị tính không đúng.", "kg", rs.getString("Donvitinh"));

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert bị trùng ID
     */
    @Test(expected = SQLException.class)
    public void testInsertNL_S19() throws Exception {
        con.setAutoCommit(false);

        ModelNguyenLieu nl1 = new ModelNguyenLieu(501, "Ớt", 30000, "kg");
        ModelNguyenLieu nl2 = new ModelNguyenLieu(501, "Tỏi", 40000, "kg");

        serviceStaff.InsertNL(nl1);
        serviceStaff.InsertNL(nl2); // Trùng ID sẽ lỗi

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert tên nguyên liệu NULL Mục đích: Phải ném SQLException vì
     * không được phép insert TenNL = null
     */
    @Test(expected = SQLException.class)
    public void testInsertNL_S20() throws Exception {
        con.setAutoCommit(false);

        ModelNguyenLieu nl = new ModelNguyenLieu(502, null, 25000, "kg");

        serviceStaff.InsertNL(nl);

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert đơn giá âm
     */
    @Test(expected = SQLException.class)
    public void testInsertNL_S21() throws Exception {
        con.setAutoCommit(false);

        ModelNguyenLieu nl = new ModelNguyenLieu(503, "Muối", -5000, "kg");

        serviceStaff.InsertNL(nl);

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert đơn vị tính sai quy định
     */
    @Test(expected = SQLException.class)
    public void testInsertNL_S22() throws Exception {
        con.setAutoCommit(false);

        ModelNguyenLieu nl = new ModelNguyenLieu(505, "Mật ong", 50000, "chai");

        serviceStaff.InsertNL(nl);

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert object null
     */
    @Test(expected = NullPointerException.class)
    public void testInsertNL_S23() throws Exception {
        serviceStaff.InsertNL(null);
    }

    /**
     * Test: Xóa nguyên liệu tồn tại
     */
    @Test
    public void testDeleteNL_S24() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        stm.execute("INSERT INTO NguyenLieu VALUES (601, 'Sả', 15000, 'kg')");

        ModelNguyenLieu nl = new ModelNguyenLieu(601, "Sả", 15000, "kg");

        serviceStaff.DeleteNL(nl);

        ResultSet rs = stm.executeQuery("SELECT * FROM NguyenLieu WHERE ID_NL = 601");
        assertFalse("Fail: Vẫn còn dữ liệu trong NguyenLieu dù đã xóa.", rs.next());

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test case: Xóa nguyên liệu không tồn tại Mong đợi: Dữ liệu trong bảng
     * NguyenLieu không bị thay đổi
     */
    @Test
    public void testDeleteNL_S25() throws Exception {
        con.setAutoCommit(false);

        // Thực hiện xóa với ID không tồn tại
        ModelNguyenLieu nl = new ModelNguyenLieu(9999, "ABC", 10000, "kg");
        serviceStaff.DeleteNL(nl);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> serviceStaff.DeleteNL(nl));
        assertEquals("Không tìm thấy nguyên liệu", ex.getMessage());

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test case: Xóa nguyên liệu với ID âm Mong đợi: Dữ liệu trong bảng
     * NguyenLieu không bị thay đổi
     */
    @Test
    public void testDeleteNL_S26() throws Exception {
        con.setAutoCommit(false);

        // Thực hiện xóa với ID âm
        ModelNguyenLieu nl = new ModelNguyenLieu(-10, "ABC", 10000, "kg");
        serviceStaff.DeleteNL(nl);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> serviceStaff.DeleteNL(nl));
        assertEquals("Id nguyên liệu không thể âm", ex.getMessage());

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Xóa với object null
     */
    @Test(expected = NullPointerException.class)
    public void testDeleteNL_S27() throws Exception {
        serviceStaff.DeleteNL(null);
    }

    //------------------------------------------------------------
// Test UpdateNL() - Cập nhật thông tin nguyên liệu
//------------------------------------------------------------
    /**
     * Test: Cập nhật dữ liệu hợp lệ Mục đích: Kiểm tra cập nhật thành công dữ
     * liệu nguyên liệu
     */
    @Test
    public void testUpdateNL_S28() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        // Insert dữ liệu ban đầu
        stm.execute("INSERT INTO NguyenLieu VALUES (700, 'Ớt', 20000, 'kg')");

        // Cập nhật lại dữ liệu
        ModelNguyenLieu nl = new ModelNguyenLieu(700, "Ớt đỏ", 30000, "l");
        serviceStaff.UpdateNL(nl);

        ResultSet rs = stm.executeQuery("SELECT * FROM NguyenLieu WHERE ID_NL = 700");

        assertTrue("Fail: Không tìm thấy dữ liệu sau khi update.", rs.next());
        assertEquals("Fail: Tên NL sai sau update.", "Ớt đỏ", rs.getString("TenNL"));
        assertEquals("Fail: Đơn giá sai sau update.", 30000, rs.getInt("DonGia"));
        assertEquals("Fail: Đơn vị tính sai sau update.", "l", rs.getString("Donvitinh"));

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Update tên nguyên liệu null Mục đích: Không được phép update TenNL
     * = null → Phải SQLException
     */
    @Test(expected = SQLException.class)
    public void testUpdateNL_S29() throws Exception {
        con.setAutoCommit(false);

        Statement stm = con.createStatement();
        stm.execute("INSERT INTO NguyenLieu VALUES (701, 'Hành', 10000, 'kg')");

        ModelNguyenLieu nl = new ModelNguyenLieu(701, null, 15000, "kg");
        serviceStaff.UpdateNL(nl);

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Update đơn giá âm Mục đích: Phải SQLException vì không được phép
     * đơn giá âm
     */
    @Test(expected = SQLException.class)
    public void testUpdateNL_S30() throws Exception {
        con.setAutoCommit(false);

        Statement stm = con.createStatement();
        stm.execute("INSERT INTO NguyenLieu VALUES (702, 'Tỏi', 10000, 'kg')");

        ModelNguyenLieu nl = new ModelNguyenLieu(702, "Tỏi", -5000, "kg");
        serviceStaff.UpdateNL(nl);

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Update đơn vị tính sai quy định Mục đích: Phải SQLException nếu đơn
     * vị tính không hợp lệ
     */
    @Test(expected = SQLException.class)
    public void testUpdateNL_S31() throws Exception {
        con.setAutoCommit(false);

        Statement stm = con.createStatement();
        stm.execute("INSERT INTO NguyenLieu VALUES (703, 'Đường', 10000, 'kg')");

        ModelNguyenLieu nl = new ModelNguyenLieu(703, "Đường", 15000, "chai");
        serviceStaff.UpdateNL(nl);

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Update object null Mục đích: Phải ném NullPointerException
     */
    @Test(expected = NullPointerException.class)
    public void testUpdateNL_S32() throws Exception {
        serviceStaff.UpdateNL(null);
    }

    /**
     * Test of MenuPNK method, of class ServiceStaff.
     */
    //có nhiều phiếu nhập kho trong danh sách
    @Test
    public void testMenuPNK_S33() throws Exception {
        ServiceStaff instance = new ServiceStaff();

        ArrayList<ModelPNK> expResult = new ArrayList<>();
        String sql = "SELECT ID_NK,ID_NV,to_char(NgayNK,'dd-mm-yyyy') AS Ngay,Tongtien FROM PhieuNK ORDER BY ID_NK";
        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int idNK = r.getInt(1);
            int idNV = r.getInt(2);
            String ngayNK = r.getString(3);
            int tongTien = r.getInt(4);
            ModelPNK data = new ModelPNK(idNK, idNV, ngayNK, tongTien);
            expResult.add(data);
        }

        ArrayList<ModelPNK> result = instance.MenuPNK();
        assertEquals("Kích thước danh sách không khớp", expResult.size(), result.size());

        for (int i = 0; i < expResult.size(); i++) {
            ModelPNK expected = expResult.get(i);
            ModelPNK actual = result.get(i);

            assertEquals("Sai ID_NK tại phần tử " + i, expected.getIdNK(), actual.getIdNK());
            assertEquals("Sai ID_NV tại phần tử " + i, expected.getIdNV(), actual.getIdNV());
            assertEquals("Sai NgayNK tại phần tử " + i, expected.getNgayNK(), actual.getNgayNK());
            assertEquals("Sai TongTien tại phần tử " + i, expected.getTongTien(), actual.getTongTien());
        }
    }

    //không có phiếu nhập kho trong danh sách
    @Test
    public void testMenuPNK_S34() throws Exception {
        try {
            // Xóa dữ liệu trong bảng
            con.setAutoCommit(false);
            String deleteSQL1 = "DELETE FROM CTNK";
            PreparedStatement deleteStmt1 = con.prepareStatement(deleteSQL1);
            deleteStmt1.executeUpdate();
            deleteStmt1.close();

            String deleteSQL = "DELETE FROM PhieuNK";
            PreparedStatement deleteStmt = con.prepareStatement(deleteSQL);
            deleteStmt.executeUpdate();
            deleteStmt.close();

            ServiceStaff instance = new ServiceStaff();

            // Gọi hàm và kỳ vọng exception được ném
            instance.MenuPNK();
            fail("Expected an Exception to be thrown");

        } catch (Exception e) {
            assertEquals("Danh sách phiếu nhập kho trống", e.getMessage());
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }

    }

    @Test
    public void testHasField_idNhacungcap_S35() {
        boolean hasField = false;
        for (Field f : ModelPNK.class.getDeclaredFields()) {
            if (f.getName().equals("id_nhacungcap")) {
                hasField = true;
                break;
            }
        }
        assertTrue("ModelPNK không có thuộc tính id_nhacungcap", hasField);
    }

    @Test
    public void testHasField_ModelPXK_S36() {
        boolean hasFieldNH = false;
        for (Field f : ModelPXK.class.getDeclaredFields()) {
            if (f.getName().equals("noiNhanHang")) {
                hasFieldNH = true;
                break;
            }

        }
        assertTrue("ModelPXK không có thuộc tính noiNhanHang", hasFieldNH);

    }

    @Test
    public void testHasField_ModelPXK_S37() {
        boolean hasFieldTT = false;
        for (Field f : ModelPXK.class.getDeclaredFields()) {
            if (f.getName().equals("tongTien")) {
                hasFieldTT = true;
                break;
            }
        }
        assertTrue("ModelPXK không có thuộc tính tongTien", hasFieldTT);
    }

    /**
     * Test of MenuKhoNL method, of class ServiceStaff.
     */
    //hiển thị đúng dữ liệu của các nguyên liệu trong kho
    @Test
    public void testMenuKhoNL_S38() throws Exception {
        ServiceStaff instance = new ServiceStaff();

        ArrayList<ModelKho> expResult = new ArrayList<>();
        String sql = "SELECT Kho.ID_NL, TenNL, Donvitinh, SLTon FROM Kho "
                + "JOIN NguyenLieu ON NguyenLieu.ID_NL = Kho.ID_NL ORDER BY Kho.ID_NL";
        PreparedStatement p = con.prepareStatement(sql);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id = r.getInt(1);
            String tenNL = r.getString(2);
            String dvt = r.getString(3);
            int slTon = r.getInt(4);
            expResult.add(new ModelKho(id, tenNL, dvt, slTon));
        }
        r.close();
        p.close();

        // Gọi hàm thực tế
        ArrayList<ModelKho> result = instance.MenuKhoNL();

        // So sánh số lượng phần tử
        assertEquals("Số lượng nguyên liệu không khớp", expResult.size(), result.size());

        // So sánh từng phần tử
        for (int i = 0; i < expResult.size(); i++) {
            ModelKho expected = expResult.get(i);
            ModelKho actual = result.get(i);
            assertEquals("ID_NL không khớp tại vị trí " + i, expected.getIdNL(), actual.getIdNL());
            assertEquals("Tên nguyên liệu không khớp tại vị trí " + i, expected.getTenNL(), actual.getTenNL());
            assertEquals("Đơn vị tính không khớp tại vị trí " + i, expected.getDvt(), actual.getDvt());
            assertEquals("Số lượng tồn không khớp tại vị trí " + i, expected.getSlTon(), actual.getSlTon());
        }
    }

    //danh sách kho trống
    @Test
    public void testMenuKhoNL_S39() throws Exception {
        try {
            con.setAutoCommit(false);

            // Xóa toàn bộ dữ liệu trong bảng Kho
            String deleteSQL = "DELETE FROM Kho";
            PreparedStatement deleteStmt = con.prepareStatement(deleteSQL);
            deleteStmt.executeUpdate();
            deleteStmt.close();

            ServiceStaff instance = new ServiceStaff();

            instance.MenuKhoNL();

            fail("Expected an Exception to be thrown");

        } catch (Exception e) {
            assertEquals("danh sách kho trống", e.getMessage());
        } finally {
            // Khôi phục dữ liệu sau test
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /**
     * Test of getSLNL_TonKho method, of class ServiceStaff.
     */
    //hiển thị đúng số lượng khi giữ nguyên kho
    @Test
    public void testGetSLNL_TonKho_S40() throws Exception {
        System.out.println("getSLNL_TonKho");
        ServiceStaff instance = new ServiceStaff();

        // Tự truy xuất DB để lấy giá trị kỳ vọng
        String sql = "SELECT COUNT(*) FROM Kho WHERE SLTon > 0";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();

        int expResult = 0;
        if (rs.next()) {
            expResult = rs.getInt(1);
        }
        rs.close();
        ps.close();

        // Gọi hàm thực tế
        int result = instance.getSLNL_TonKho();

        // So sánh
        assertEquals("Số lượng nguyên liệu còn tồn không khớp", expResult, result);
    }

    //hiển thị đúng khi thay đổi 1 nguyên liệu có số lượng > 0 thành 0
    @Test
    public void testGetSLNL_TonKho_S41() throws Exception {
        ServiceStaff instance = new ServiceStaff();

        try {
            // Tạm tắt tự động commit để rollback được
            con.setAutoCommit(false);

            // Cập nhật SLTon của ID_NL = 102 thành 0
            String updateSQL = "UPDATE Kho SET SLTon = 0 WHERE ID_NL = 102";
            PreparedStatement updateStmt = con.prepareStatement(updateSQL);
            updateStmt.executeUpdate();
            updateStmt.close();

            // Tự truy vấn lại DB để lấy expected
            String sql = "SELECT COUNT(*) FROM Kho WHERE SLTon > 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int expResult = 0;
            if (rs.next()) {
                expResult = rs.getInt(1);
            }
            rs.close();
            ps.close();

            // Gọi hàm thực tế
            int result = instance.getSLNL_TonKho();

            // So sánh
            assertEquals("Số lượng nguyên liệu tồn không đúng sau khi cập nhật SLTon", expResult, result);

        } finally {
            // Khôi phục lại dữ liệu gốc
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    //hiển thị đúng khi thay đổi 1 nguyên liệu có số lượng = 0 thành > 0
    @Test
    public void testGetSLNL_TonKho_S42() throws Exception {
        ServiceStaff instance = new ServiceStaff();

        try {
            // Tạm tắt tự động commit để rollback được
            con.setAutoCommit(false);

            // Cập nhật SLTon của ID_NL = 111 thành 0
            String updateSQL = "UPDATE Kho SET SLTon = 5 WHERE ID_NL = 111";
            PreparedStatement updateStmt = con.prepareStatement(updateSQL);
            updateStmt.executeUpdate();
            updateStmt.close();

            // Tự truy vấn lại DB để lấy expected
            String sql = "SELECT COUNT(*) FROM Kho WHERE SLTon > 0";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            int expResult = 0;
            if (rs.next()) {
                expResult = rs.getInt(1);
            }
            rs.close();
            ps.close();

            // Gọi hàm thực tế
            int result = instance.getSLNL_TonKho();

            // So sánh
            assertEquals("Số lượng nguyên liệu tồn không đúng sau khi cập nhật SLTon", expResult, result);

        } finally {
            // Khôi phục lại dữ liệu gốc
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    @Test
    public void testGetSLNL_TonKho_S43() throws Exception {
        ServiceStaff instance = new ServiceStaff();
        try {
            // Tắt auto-commit để rollback được
            con.setAutoCommit(false);

            // Cập nhật tất cả nguyên liệu trong kho: SLTon = 0
            String updateSQL = "UPDATE Kho SET SLTon = 0";
            PreparedStatement updateStmt = con.prepareStatement(updateSQL);
            updateStmt.executeUpdate();
            updateStmt.close();

            // Expected: không còn nguyên liệu tồn (>0) → COUNT = 0
            int expResult = 0;

            // Gọi hàm cần test
            int result = instance.getSLNL_TonKho();

            // So sánh
            assertEquals("Hàm không trả về 0 khi kho trống hoàn toàn", expResult, result);

        } finally {
            // Rollback để khôi phục dữ liệu gốc
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    // test xem thông tin khách hàng 
    @Test
    public void testMenuKH_S44() throws SQLException {
        ArrayList<ModelKhachHang> expResult = new ArrayList<>();
        String sql = "SELECT ID_KH, TenKH, to_char(Ngaythamgia,'dd-mm-yyyy'), Doanhso, Diemtichluy FROM KhachHang";
        PreparedStatement ps = con.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int id = rs.getInt(1);
            String name = rs.getString(2);
            String date = rs.getString(3);
            int sales = rs.getInt(4);
            int points = rs.getInt(5);
            expResult.add(new ModelKhachHang(id, name, date, sales, points));
        }
        rs.close();
        ps.close();

        // Gọi hàm thực tế
        ArrayList<ModelKhachHang> result = serviceStaff.MenuKH();

        // So sánh số lượng
        assertEquals("Số lượng khách hàng không khớp", expResult.size(), result.size());

        // So sánh từng khách hàng
        for (int i = 0; i < expResult.size(); i++) {
            ModelKhachHang expected = expResult.get(i);
            ModelKhachHang actual = result.get(i);

            assertEquals("ID_KH không khớp tại vị trí " + i, expected.getID_KH(), actual.getID_KH());
            assertEquals("Tên không khớp tại vị trí " + i, expected.getName(), actual.getName());
            assertEquals("Ngày tham gia không khớp tại vị trí " + i, expected.getDateJoin(), actual.getDateJoin());
            assertEquals("Doanh số không khớp tại vị trí " + i, expected.getSales(), actual.getSales());
            assertEquals("Điểm tích lũy không khớp tại vị trí " + i, expected.getPoints(), actual.getPoints());
        }
    }

    // test xem thông tin khách hàng khi danh sách trống
    @Test
    public void testMenuKH_S45() throws SQLException {
        try {
            // Tắt auto-commit để có thể rollback
            con.setAutoCommit(false);

            // BƯỚC 1: Xóa tất cả chi tiết hóa đơn
            String deleteCTHD = "DELETE FROM CTHD";
            PreparedStatement stmtCTHD = con.prepareStatement(deleteCTHD);
            stmtCTHD.executeUpdate();
            stmtCTHD.close();

            // BƯỚC 2: Xóa tất cả hóa đơn
            String deleteHD = "DELETE FROM HoaDon";
            PreparedStatement stmtHD = con.prepareStatement(deleteHD);
            stmtHD.executeUpdate();
            stmtHD.close();
            // Xóa toàn bộ khách hàng
            String deleteSQL = "DELETE FROM KhachHang";
            PreparedStatement ps = con.prepareStatement(deleteSQL);
            ps.executeUpdate();
            ps.close();

            // Gọi hàm và kỳ vọng ném ra Exception
            serviceStaff.MenuKH();
            fail("Expected Exception to be thrown vì danh sách rỗng");

        } catch (Exception e) {
            assertEquals("danh sách khách hàng trống", e.getMessage());
        } finally {
            // Khôi phục lại dữ liệu
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    //------------------------------------------------------------
    // Test setTableReserve - Đặt trước bàn
    //------------------------------------------------------------
    /**
     * Test: Đặt trước bàn hợp lệ
     */
    @Test
    public void testSetTableReserve_S46() throws Exception {
        int idBan = 100;
        con.setAutoCommit(false);
        serviceStaff.setTableReserve(idBan);

        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery("SELECT TrangThai FROM Ban WHERE ID_Ban = 100");

        if (rs.next()) {
            assertEquals("Da dat truoc", rs.getString("TrangThai"));
        } else {
            fail("Không tìm thấy bàn để kiểm tra trạng thái");
        }

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Đặt trước bàn không tồn tại
     */
    @Test
    public void testSetTableReserve_S47() throws Exception {
        int idBan = 9999; // ID bàn không tồn tại

        try {
            con.setAutoCommit(false);

            // Gọi phương thức, kỳ vọng ném Exception
            serviceStaff.setTableReserve(idBan);

            // Nếu không ném lỗi thì fail
            fail("Expected Exception to be thrown vì ID bàn không tồn tại");

        } catch (Exception e) {
            // So sánh nội dung Exception
            assertEquals("không tìm thấy bàn", e.getMessage());
        } finally {
            con.rollback();           // Khôi phục dữ liệu
            con.setAutoCommit(true);
        }
    }

    /**
     * Test: Đặt trước bàn với ID = 0
     */
    @Test
    public void testSetTableReserve_S48() throws Exception {
        int idBan = 0;
        try {
            con.setAutoCommit(false);

            // Gọi phương thức, kỳ vọng ném Exception
            serviceStaff.setTableReserve(idBan);

            // Nếu không ném lỗi thì fail
            fail("Expected Exception to be thrown vì ID bàn >=100");

        } catch (Exception e) {
            // So sánh nội dung Exception
            assertEquals("ID bàn phải >=100", e.getMessage());
        } finally {
            con.rollback();           // Khôi phục dữ liệu
            con.setAutoCommit(true);
        }
    }

    //------------------------------------------------------------
    // Test CancelTableReserve - Hủy đặt trước bàn
    //------------------------------------------------------------
    /**
     * Test: Bàn đang "Đã đặt trước" -> chuyển về "Còn trống"
     */
    @Test
    public void testCancelTableReserve_S49() throws Exception {
        int idBan = 101;
        con.setAutoCommit(false);

        Statement stm = con.createStatement();
        stm.execute("UPDATE Ban SET TrangThai = 'Da dat truoc' WHERE ID_Ban = " + idBan);

        serviceStaff.CancelTableReserve(idBan);

        ResultSet rs = stm.executeQuery("SELECT TrangThai FROM Ban WHERE ID_Ban = " + idBan);

        if (rs.next()) {
            assertEquals("Con trong", rs.getString("TrangThai"));
        } else {
            fail("Không tìm thấy bàn sau khi cập nhật");
        }

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Bàn đã "Còn trống" sẵn
     */
    @Test
    public void testCancelTableReserve_S50() throws Exception {
        int idBan = 102;
        try {
            con.setAutoCommit(false);

            // Đặt trạng thái bàn là "Còn trống" trước
            Statement stm = con.createStatement();
            stm.executeUpdate("UPDATE Ban SET TrangThai = 'Con trong' WHERE ID_Ban = " + idBan);
            stm.close();

            // Gọi phương thức, kỳ vọng ném Exception
            serviceStaff.CancelTableReserve(idBan);
            fail("Expected Exception to be thrown vì bàn 'Còn trống'");

        } catch (Exception e) {
            assertEquals("trạng thái bàn đang là còn trống", e.getMessage());
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /**
     * Test: Hủy đặt trước bàn không tồn tại
     */
    @Test
    public void testCancelTableReserve_S51() throws Exception {
        int idBan = 9999;
        try {
            con.setAutoCommit(false);
            // Gọi phương thức, kỳ vọng ném Exception
            serviceStaff.CancelTableReserve(idBan);
            fail("Expected Exception to be thrown vì bàn không tồn tại");

        } catch (Exception e) {
            assertEquals("không tìm thấy bàn", e.getMessage());
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /**
     * Test: Hủy đặt trước bàn với ID âm
     */
    @Test
    public void testCancelTableReserve_S52() throws Exception {
        int idBan = -1;
        try {
            con.setAutoCommit(false);
            // Gọi phương thức, kỳ vọng ném Exception
            serviceStaff.CancelTableReserve(idBan);
            fail("Expected Exception to be thrown vì id bàn >=100");

        } catch (Exception e) {
            assertEquals("ID bàn phải >=100", e.getMessage());
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /**
     * Test: Bàn có hóa đơn chưa thanh toán Mục đích: Kiểm tra tìm đúng hóa đơn
     * theo ID bàn
     */
    @Test
    public void testFindHoaDonbyIDBan_S53() throws Exception {
        try {
            con.setAutoCommit(false);
            ModelBan table = new ModelBan(102, "Bàn 3");
            table.setStatus("Trống");
            ServiceCustomer s = new ServiceCustomer();
            ModelKhachHang kh = s.getCustomer(112);
            s.InsertHoaDon(table, kh);

            ModelHoaDon hd = serviceStaff.FindHoaDonbyID_Ban(table);

            assertNotNull("Phải tìm được hóa đơn chưa thanh toán theo ID_Ban", hd);
            assertEquals("Sai ID bàn", table.getID(), hd.getIdBan());
            assertEquals("Sai ID khách hàng", kh.getID_KH(), hd.getIdKH());
            assertNotNull("Ngày hóa đơn không được null", hd.getNgayHD());
            assertEquals("Tiền món ăn ban đầu phải là 0", 0, hd.getTienMonAn());
            assertNull("Mã voucher ban đầu phải là null", hd.getCode_voucher());
            assertEquals("Tiền giảm ban đầu phải là 0", 0, hd.getTienGiam());
            assertEquals("Tổng tiền ban đầu phải là 0", 0, hd.getTongtien());
            assertEquals("Trạng thái phải là 'Chua thanh toan'", "Chua thanh toan", hd.getTrangthai());

        } catch (Exception e) {
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /**
     * Test: Bàn hợp lệ, nhưng không có hóa đơn chưa thanh toán
     */
    @Test
    public void testFindHoaDonbyIDBan_S54() throws Exception {
        try {
            con.setAutoCommit(false);
            ModelBan table = new ModelBan(102, "Bàn 3");
            table.setStatus("Trống");

            ModelHoaDon hd = serviceStaff.FindHoaDonbyID_Ban(table);

            assertNull("Phải không có hóa đơn chưa thanh toán theo ID_Ban", hd);

        } catch (Exception e) {
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /**
     * Test: Bàn không tồn tại
     */
    @Test
    public void testFindHoaDonbyIDBan_S55() throws Exception {
        try {
            con.setAutoCommit(false);
            ModelBan table = new ModelBan(9999, "Bàn 3");
            table.setStatus("Trống");
            ServiceCustomer s = new ServiceCustomer();
            ModelKhachHang kh = s.getCustomer(112);
            s.InsertHoaDon(table, kh);

            ModelHoaDon hd = serviceStaff.FindHoaDonbyID_Ban(table);
            assertNull("Phải không có hóa đơn chưa thanh toán theo ID_Ban", hd);
        } catch (Exception e) {
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    @Test
    public void testFindHoaDonbyIDBan_S56() throws Exception {
        try {
            con.setAutoCommit(false);
            ModelBan table = new ModelBan(0, "Bàn 3");
            table.setStatus("Trống");
            ServiceCustomer s = new ServiceCustomer();
            ModelKhachHang kh = s.getCustomer(112);
            s.InsertHoaDon(table, kh);

            ModelHoaDon hd = serviceStaff.FindHoaDonbyID_Ban(table);
            assertNull("Phải không có hóa đơn chưa thanh toán theo ID_Ban", hd);
        } catch (Exception e) {
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /**
     * Test: Tham số truyền vào là null
     */
    @Test(expected = NullPointerException.class)
    public void testFindHoaDonbyIDBan_S57() throws Exception {
        serviceStaff.FindHoaDonbyID_Ban(null);
    }

    //------------------------------------------------------------
    // Test UpdateHoaDonStatus - Cập nhật trạng thái hóa đơn
    //------------------------------------------------------------
    /**
     * Test: Cập nhật hóa đơn chưa thanh toán -> Đã thanh toán
     */
    @Test
    public void testUpdateHoaDonStatus_S58() throws Exception {
        try {
            con.setAutoCommit(false);
            ModelBan table = new ModelBan(102, "Bàn 3");
            table.setStatus("Trống");
            ServiceCustomer s = new ServiceCustomer();
            ModelKhachHang kh = s.getCustomer(112);
            s.InsertHoaDon(table, kh);

            ModelHoaDon hd = serviceStaff.FindHoaDonbyID_Ban(table);
            serviceStaff.UpdateHoaDonStatus(hd.getIdHoaDon());

            ModelHoaDon hdResult = serviceStaff.FindHoaDonbyID_Ban(table);

            assertEquals("Trạng thái phải là 'Da thanh toan'", "Da thanh toan", hdResult.getTrangthai());

        } catch (Exception e) {
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /**
     * Test: Hóa đơn đã thanh toán trước đó
     */
    @Test
    public void testUpdateHoaDonStatus_S59() throws Exception {
        try {
            con.setAutoCommit(false);
            int idHD = 109;

            serviceStaff.UpdateHoaDonStatus(idHD);
            fail("mong chờ exception");
        } catch (Exception e) {
            assertEquals("hóa đơn đã thanh toán", e.getMessage());
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /**
     * Test: Cập nhật hóa đơn không tồn tại
     */
    @Test
    public void testUpdateHoaDonStatus_S60() throws Exception {
        try {
            con.setAutoCommit(false);
            int idHD = 9999;

            serviceStaff.UpdateHoaDonStatus(idHD);
            fail("mong chờ exception");
        } catch (Exception e) {
            assertEquals("hóa đơn không tồn tại", e.getMessage());
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /**
     * Test: Cập nhật hóa đơn với ID âm
     */
    @Test
    public void testUpdateHoaDonStatus_S61() throws Exception {
        try {
            con.setAutoCommit(false);
            int idHD = -10;

            serviceStaff.UpdateHoaDonStatus(idHD);
            fail("mong chờ exception");
        } catch (Exception e) {
            assertEquals("id hóa đơn phải >=100", e.getMessage());
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /**
     * Test of getTenKH method, of class ServiceStaff.
     */
    @Test
    public void testGetTenKH_S62() throws Exception {
        int idUser = 104;
        ServiceCustomer s = new ServiceCustomer();
        ModelKhachHang kh = s.getCustomer(idUser);
        assertEquals(kh.getName(), serviceStaff.getTenKH(kh.getID_KH()));
    }

    @Test
    public void testGetTenKH_S63() throws Exception {
        try {
            int id = 9999;
            serviceStaff.getTenKH(id);
            fail("mong chờ exception");
        } catch (Exception e) {
            assertEquals("không tìm thấy khách hàng", e.getMessage());
        }

    }

    @Test
    public void testGetTenKH_S64() throws Exception {
        try {
            int id = -99;
            serviceStaff.getTenKH(id);
            fail("mong chờ exception");
        } catch (Exception e) {
            assertEquals("id khách hàng phải >=100", e.getMessage());
        }

    }
}
