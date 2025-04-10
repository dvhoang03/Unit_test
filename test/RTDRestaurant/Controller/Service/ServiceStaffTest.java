/*
 * Class ServiceStaffTest
 * Mục đích: Thực hiện kiểm thử (unit test) cho các hàm trong class ServiceStaff
 * Bao gồm:
 * - Tìm hóa đơn theo ID Bàn
 * - Đặt trước bàn
 * - Hủy đặt trước bàn
 * - Cập nhật trạng thái hóa đơn
 * - Lấy danh sách nguyên liệu (MenuNL)
 * 
 * Công cụ sử dụng:
 * - JUnit 4
 * - Java SQL Connection thật
 * - Rollback sau mỗi test để đảm bảo dữ liệu không ảnh hưởng DB thật
 */
package RTDRestaurant.Controller.Service;

import java.sql.ResultSet;
import RTDRestaurant.Controller.Connection.DatabaseConnection;
import RTDRestaurant.Model.ModelBan;
import RTDRestaurant.Model.ModelHoaDon;
import RTDRestaurant.Model.ModelKhachHang;
import RTDRestaurant.Model.ModelNguyenLieu;
import java.sql.*;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ServiceStaffTest {

    ServiceStaff serviceStaff;
    private Connection con;
    private ServiceStaff ss;

    public ServiceStaffTest() {
    }

    /**
     * Thiết lập kết nối DB và khởi tạo đối tượng ServiceStaff
     */
    @Before
    public void setUp() throws Exception {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        dbConnection.connectToDatabase();
        con = dbConnection.getConnection();
        serviceStaff = new ServiceStaff();

        ss = new ServiceStaff();
    }

    //------------------------------------------------------------
    // Test FindHoaDonbyID_Ban
    //------------------------------------------------------------
    /**
     * Test: Bàn có hóa đơn chưa thanh toán Mục đích: Kiểm tra tìm đúng hóa đơn
     * theo ID bàn
     */
    @Test
    public void testFindHoaDonbyIDBan_HasUnpaidBill() throws Exception {
        ModelBan table = new ModelBan(102, "Bàn 3");
        table.setStatus("Trống");

        ModelHoaDon hd = serviceStaff.FindHoaDonbyID_Ban(table);

        assertNotNull("Phải tìm được hóa đơn chưa thanh toán theo ID_Ban", hd);
        assertEquals(table.getID(), hd.getIdBan());
        assertEquals("Chua thanh toan", hd.getTrangthai());
    }

    /**
     * Test: Bàn hợp lệ, nhưng không có hóa đơn chưa thanh toán
     */
    @Test
    public void testFindHoaDonbyIDBan_NoUnpaidBill() throws Exception {
        ModelBan table = new ModelBan(103, "Bàn 4");
        table.setStatus("Trống");

        ModelHoaDon hd = serviceStaff.FindHoaDonbyID_Ban(table);

        assertNull("Không được trả về hóa đơn nếu bàn không có hóa đơn chưa thanh toán", hd);
    }

    /**
     * Test: Bàn không tồn tại
     */
    @Test
    public void testFindHoaDonbyIDBan_TableNotExist() throws Exception {
        ModelBan fakeTable = new ModelBan(999, "Bàn ảo");
        fakeTable.setStatus("Trống");

        ModelHoaDon hd = serviceStaff.FindHoaDonbyID_Ban(fakeTable);

        assertNull("Không được tìm thấy hóa đơn nếu bàn không tồn tại", hd);
    }

    /**
     * Test: Tham số truyền vào là null
     */
    @Test(expected = NullPointerException.class)
    public void testFindHoaDonbyIDBan_NullTable() throws Exception {
        serviceStaff.FindHoaDonbyID_Ban(null);
    }

    //------------------------------------------------------------
    // Test setTableReserve - Đặt trước bàn
    //------------------------------------------------------------
    /**
     * Test: Đặt trước bàn hợp lệ
     */
    @Test
    public void testSetTableReserve_ValidID() throws Exception {
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
    public void testSetTableReserve_TableNotExist() throws Exception {
        int idBan = 9999;
        con.setAutoCommit(false);
        serviceStaff.setTableReserve(idBan);

        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery("SELECT TrangThai FROM Ban WHERE ID_Ban = " + idBan);

        assertFalse("Không nên tìm thấy bàn với ID không tồn tại", rs.next());

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Đặt trước bàn với ID = 0
     */
    @Test
    public void testSetTableReserve_ZeroID() throws Exception {
        int idBan = 0;
        con.setAutoCommit(false);
        serviceStaff.setTableReserve(idBan);

        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery("SELECT TrangThai FROM Ban WHERE ID_Ban = " + idBan);

        assertFalse("Không nên có bàn nào với ID = 0", rs.next());

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Đặt trước bàn với ID âm
     */
    @Test
    public void testSetTableReserve_NegativeID() throws Exception {
        int idBan = -5;
        con.setAutoCommit(false);
        serviceStaff.setTableReserve(idBan);

        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery("SELECT TrangThai FROM Ban WHERE ID_Ban = " + idBan);

        assertFalse("Không nên có bàn nào với ID âm", rs.next());

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    //------------------------------------------------------------
    // Test CancelTableReserve - Hủy đặt trước bàn
    //------------------------------------------------------------
    /**
     * Test: Bàn đang "Đã đặt trước" -> chuyển về "Còn trống"
     */
    @Test
    public void testCancelTableReserve_ValidReservedTable() throws Exception {
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
    public void testCancelTableReserve_AlreadyAvailable() throws Exception {
        int idBan = 102;
        con.setAutoCommit(false);

        Statement stm = con.createStatement();
        stm.execute("UPDATE Ban SET TrangThai = 'Con trong' WHERE ID_Ban = " + idBan);

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
     * Test: Hủy đặt trước bàn không tồn tại
     */
    @Test
    public void testCancelTableReserve_TableNotExist() throws Exception {
        int idBan = 9999;
        con.setAutoCommit(false);
        serviceStaff.CancelTableReserve(idBan);

        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery("SELECT TrangThai FROM Ban WHERE ID_Ban = " + idBan);

        assertFalse("Không nên có bàn nào với ID không tồn tại", rs.next());

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Hủy đặt trước bàn với ID âm
     */
    @Test
    public void testCancelTableReserve_InvalidID() throws Exception {
        int idBan = -1;
        con.setAutoCommit(false);
        serviceStaff.CancelTableReserve(idBan);

        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery("SELECT TrangThai FROM Ban WHERE ID_Ban = " + idBan);

        assertFalse("Không nên có bàn nào với ID âm", rs.next());

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    //------------------------------------------------------------
    // Test UpdateHoaDonStatus - Cập nhật trạng thái hóa đơn
    //------------------------------------------------------------
    /**
     * Test: Cập nhật hóa đơn chưa thanh toán -> Đã thanh toán
     */
    @Test
    public void testUpdateHoaDonStatus_ValidID() throws Exception {
        int idHD = 123;
        con.setAutoCommit(false);

        Statement stm = con.createStatement();
        stm.execute("UPDATE HoaDon SET TrangThai = 'Chua thanh toan' WHERE ID_HoaDon = " + idHD);

        serviceStaff.UpdateHoaDonStatus(idHD);

        ResultSet rs = stm.executeQuery("SELECT TrangThai FROM HoaDon WHERE ID_HoaDon = " + idHD);

        if (rs.next()) {
            assertEquals("Da thanh toan", rs.getString("TrangThai"));
        } else {
            fail("Không tìm thấy hóa đơn sau khi cập nhật");
        }

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Hóa đơn đã thanh toán trước đó
     */
    @Test
    public void testUpdateHoaDonStatus_AlreadyPaid() throws Exception {
        int idHD = 101;
        con.setAutoCommit(false);

        Statement stm = con.createStatement();
        stm.execute("UPDATE HoaDon SET TrangThai = 'Da thanh toan' WHERE ID_HoaDon = " + idHD);

        serviceStaff.UpdateHoaDonStatus(idHD);

        ResultSet rs = stm.executeQuery("SELECT TrangThai FROM HoaDon WHERE ID_HoaDon = " + idHD);

        if (rs.next()) {
            assertEquals("Da thanh toan", rs.getString("TrangThai"));
        }

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Cập nhật hóa đơn không tồn tại
     */
    @Test
    public void testUpdateHoaDonStatus_NotExist() throws Exception {
        int idHD = 9999;
        con.setAutoCommit(false);

        serviceStaff.UpdateHoaDonStatus(idHD);

        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery("SELECT TrangThai FROM HoaDon WHERE ID_HoaDon = " + idHD);

        assertFalse("Không nên có hóa đơn với ID không tồn tại", rs.next());

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Cập nhật hóa đơn với ID âm
     */
    @Test
    public void testUpdateHoaDonStatus_InvalidID() throws Exception {
        int idHD = -10;
        con.setAutoCommit(false);

        serviceStaff.UpdateHoaDonStatus(idHD);

        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery("SELECT * FROM HoaDon WHERE ID_HoaDon = " + idHD);

        assertFalse("Không nên có hóa đơn với ID âm", rs.next());

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    //------------------------------------------------------------
    // Test MenuNL() - Lấy danh sách nguyên liệu (chuẩn DB thật)
    //------------------------------------------------------------
    /**
     * Test case kiểm tra hàm MenuNL() Trường hợp: Bảng NguyenLieu có dữ liệu
     * Mục đích: Kiểm tra lấy đúng dữ liệu nguyên liệu đã insert
     */
    @Test
    public void testMenuNL_HasData() throws Exception {
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
     * Test case kiểm tra hàm MenuNL() Trường hợp: Bảng NguyenLieu không có dữ
     * liệu test thêm Mục đích: Kiểm tra lấy dữ liệu, không có ID >= 200
     */
    @Test
    public void testMenuNL_EmptyTable() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        stm.execute("DELETE FROM NguyenLieu WHERE ID_NL >= 200");

        ArrayList<ModelNguyenLieu> list = serviceStaff.MenuNL();

        assertTrue(list.stream().noneMatch(x -> x.getId() >= 200));

        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test case kiểm tra hàm MenuNL() Trường hợp: Insert dữ liệu có đơn giá = 0
     * Mục đích: Kiểm tra KHÔNG lấy dữ liệu có đơn giá = 0
     */
    @Test
    public void testMenuNL_ExcludeZeroPrice() throws Exception {
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
    public void testMenuNL_ExcludeNegativePrice() throws Exception {
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

    //------------------------------------------------------------
// Test getNLbyID() - Lấy thông tin nguyên liệu theo ID
//------------------------------------------------------------
    /**
     * Test case kiểm tra hàm getNLbyID() Trường hợp: ID nguyên liệu tồn tại Mục
     * đích: Kiểm tra lấy đúng dữ liệu theo ID
     */
    @Test
    public void testGetNLbyID_Exist() throws Exception {
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
    public void testGetNLbyID_NotExist() throws Exception {
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
    public void testGetNLbyID_ZeroID() throws Exception {
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
    public void testGetNLbyID_NegativeID() throws Exception {
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
    public void testGetNextID_NL_WithData() throws Exception {
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
    public void testGetNextID_NL_LargeID() throws Exception {
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
    public void testGetNextID_NL_SQLException() throws Exception {
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
    public void testInsertNL_ValidData() throws Exception {
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
    public void testInsertNL_DuplicateID() throws Exception {
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
    public void testInsertNL_NullName() throws Exception {
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
    public void testInsertNL_NegativePrice() throws Exception {
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
    public void testInsertNL_InvalidUnit() throws Exception {
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
    public void testInsertNL_NullObject() throws Exception {
        serviceStaff.InsertNL(null);
    }

    /**
     * Test: Xóa nguyên liệu tồn tại
     */
    @Test
    public void testDeleteNL_ExistNotInKho() throws Exception {
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
    public void testDeleteNL_NotExist() throws Exception {
        con.setAutoCommit(false);

        // Đếm số lượng bản ghi ban đầu
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery("SELECT COUNT(*) FROM NguyenLieu");
        rs.next();
        int countBefore = rs.getInt(1);

        // Thực hiện xóa với ID không tồn tại
        ModelNguyenLieu nl = new ModelNguyenLieu(9999, "ABC", 10000, "kg");
        serviceStaff.DeleteNL(nl);

        // Đếm lại số lượng bản ghi sau khi xóa
        rs = stm.executeQuery("SELECT COUNT(*) FROM NguyenLieu");
        rs.next();
        int countAfter = rs.getInt(1);

        // So sánh
        assertEquals("Fail: Xóa ID không tồn tại nhưng số lượng bản ghi lại thay đổi.", countBefore, countAfter);

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test case: Xóa nguyên liệu với ID âm Mong đợi: Dữ liệu trong bảng
     * NguyenLieu không bị thay đổi
     */
    @Test
    public void testDeleteNL_NegativeID() throws Exception {
        con.setAutoCommit(false);

        // Đếm số lượng bản ghi ban đầu
        Statement stm = con.createStatement();
        ResultSet rs = stm.executeQuery("SELECT COUNT(*) FROM NguyenLieu");
        rs.next();
        int countBefore = rs.getInt(1);

        // Thực hiện xóa với ID âm
        ModelNguyenLieu nl = new ModelNguyenLieu(-10, "ABC", 10000, "kg");
        serviceStaff.DeleteNL(nl);

        // Đếm lại số lượng bản ghi sau khi xóa
        rs = stm.executeQuery("SELECT COUNT(*) FROM NguyenLieu");
        rs.next();
        int countAfter = rs.getInt(1);

        // So sánh
        assertEquals("Fail: Xóa ID âm nhưng số lượng bản ghi lại thay đổi.", countBefore, countAfter);

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Xóa với object null
     */
    @Test(expected = NullPointerException.class)
    public void testDeleteNL_NullObject() throws Exception {
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
    public void testUpdateNL_ValidData() throws Exception {
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
    public void testUpdateNL_NullName() throws Exception {
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
    public void testUpdateNL_NegativePrice() throws Exception {
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
    public void testUpdateNL_InvalidUnit() throws Exception {
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
    public void testUpdateNL_NullObject() throws Exception {
        serviceStaff.UpdateNL(null);
    }

    /*
        getNLByID()
     */
    @Test
    public void testGetNLbyID_hopLe() throws SQLException {
        //ID hợp lệ bắt đầu từ 100 - 115
        ModelNguyenLieu test = ss.getNLbyID(100);
        assertEquals(100, test.getId());
    }

    @Test
    public void testGetNLbyID_IdKhongTonTai() throws SQLException {
        //ID hợp lệ bắt đầu từ 100 - 115
        ModelNguyenLieu test = ss.getNLbyID(10);
        assertNull(test);
    }

    @Test
    public void testGetNLbyID_IdLaSoAm() throws SQLException {
        //ID hợp lệ bắt đầu từ 100 - 115
        ModelNguyenLieu test = ss.getNLbyID(-10);
        assertNull(test);
    }

    @Test
    public void testGetNLbyID_IdBangKhong() throws SQLException {
        //ID hợp lệ bắt đầu từ 100 - 115
        ModelNguyenLieu test = ss.getNLbyID(0);
        assertNull(test);
    }

    // test xem thông tin khách hàng
    @Test
    public void testMenuKH() throws SQLException {
        // test khi trong cơ sở dữ liệu là 10 khách hàng
        ArrayList<ModelKhachHang> result = ss.MenuKH();

        // Kiểm tra kết quả trả về
        assertNotNull(result);
        assertEquals(10, result.size()); //  có 10 khách hàng trong danh sách
    }
}
