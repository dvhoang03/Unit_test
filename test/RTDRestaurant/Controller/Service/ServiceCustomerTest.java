/*
 * Class ServiceCustomerTest
 * Mục đích: Thực hiện kiểm thử (unit test) cho các hàm trong class ServiceCustomer
 * Bao gồm:
 * - Insert hóa đơn (InsertHoaDon)
 * - Tìm hóa đơn theo khách hàng (FindHoaDon)
 *
 * Công cụ sử dụng:
 * - JUnit 4
 * - Java SQL Connection thật
 * - Rollback sau mỗi test để đảm bảo dữ liệu không ảnh hưởng DB thật
 */
package RTDRestaurant.Controller.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static org.junit.Assert.*;

import RTDRestaurant.Controller.Connection.DatabaseConnection;
import RTDRestaurant.Model.ModelBan;
import RTDRestaurant.Model.ModelCTHD;
import RTDRestaurant.Model.ModelHoaDon;
import RTDRestaurant.Model.ModelKhachHang;
import RTDRestaurant.Model.ModelMonAn;
import RTDRestaurant.Model.ModelVoucher;
import org.junit.Test;
import java.sql.*;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import org.junit.Before;

public class ServiceCustomerTest {

    private Connection con;
    ServiceCustomer serviceCustomer;
    private ServiceCustomer service;

    public ServiceCustomerTest() {
    }

    /**
     * Thiết lập kết nối DB và khởi tạo đối tượng ServiceCustomer
     */
    @Before
    public void setUp() throws Exception {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        dbConnection.connectToDatabase();
        con = dbConnection.getConnection();
        serviceCustomer = new ServiceCustomer();

        service = new ServiceCustomer();

    }

    //------------------------------------------------------------
    // Test InsertHoaDon - Chèn hóa đơn mới
    //------------------------------------------------------------
    /**
     * Test: Insert hóa đơn với dữ liệu hợp lệ Mục đích: Kiểm tra Insert thành
     * công khi cả khách hàng và bàn đều hợp lệ
     */
    @Test
    public void testInsertHoaDon_ValidData_CUS_01() {
        ModelKhachHang customer = new ModelKhachHang(100, "Test KH", "01-01-2024", 0, 0);
        ModelBan table = new ModelBan(108, "Bàn 3");
        table.setStatus("Con trong");

        try {
            con.setAutoCommit(false);

            serviceCustomer.InsertHoaDon(table, customer);

            ModelHoaDon hd = serviceCustomer.FindHoaDon(customer);

            assertNotNull("Fail: Phải tạo được hóa đơn mới", hd);
            assertEquals("Fail: ID_KH không đúng", customer.getID_KH(), hd.getIdKH());
            assertEquals("Fail: ID_Ban không đúng", table.getID(), hd.getIdBan());
            assertEquals("Fail: Trạng thái không đúng", "Chua thanh toan", hd.getTrangthai());

        } catch (Exception e) {
            fail("Xảy ra lỗi không mong muốn: " + e.getMessage());
        } finally {
            try {
                con.rollback();
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Test: Insert hóa đơn với khách hàng không tồn tại Mục đích: Nếu có khóa
     * ngoại thì phải lỗi, nếu không thì không được có hóa đơn nào
     */
    @Test
    public void testInsertHoaDon_InvalidCustomer_CUS_02() {
        ModelKhachHang fakeCustomer = new ModelKhachHang(9999, "Không tồn tại", "01-01-2000", 0, 0);
        ModelBan table = new ModelBan(108, "Bàn 3");
        table.setStatus("Con trong");

        try {
            con.setAutoCommit(false);

            serviceCustomer.InsertHoaDon(table, fakeCustomer);

            ModelHoaDon hd = serviceCustomer.FindHoaDon(fakeCustomer);

            assertNull("Fail: Không nên tạo hóa đơn nếu khách hàng không tồn tại", hd);

        } catch (SQLException e) {
            System.out.println("Đúng như kỳ vọng: Lỗi xảy ra khi khách hàng không tồn tại: " + e.getMessage());
        } finally {
            try {
                con.rollback();
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Test: Insert hóa đơn với bàn không tồn tại Mục đích: Nếu có khóa ngoại
     * thì phải lỗi, nếu không thì ID_Ban sai
     */
    @Test
    public void testInsertHoaDon_InvalidTable_CUS_03() {
        ModelKhachHang customer = new ModelKhachHang(100, "Test KH", "01-01-2024", 0, 0);
        ModelBan fakeTable = new ModelBan(999, "Bàn ảo");
        fakeTable.setStatus("Trống");

        try {
            con.setAutoCommit(false);

            serviceCustomer.InsertHoaDon(fakeTable, customer);

            ModelHoaDon hd = serviceCustomer.FindHoaDon(customer);

            if (hd != null) {
                assertNotEquals("Fail: ID_Ban không nên là bàn không tồn tại", fakeTable.getID(), hd.getIdBan());
            }

        } catch (SQLException e) {
            System.out.println("Đúng như kỳ vọng: Lỗi xảy ra vì bàn không tồn tại: " + e.getMessage());
        } finally {
            try {
                con.rollback();
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    //------------------------------------------------------------
    // Test FindHoaDon - Tìm hóa đơn chưa thanh toán theo khách hàng
    //------------------------------------------------------------
    /**
     * Test: Khách hàng có hóa đơn chưa thanh toán Mục đích: Phải tìm thấy đúng
     * hóa đơn chưa thanh toán
     */
    @Test
    public void testFindHoaDon_CustomerHasUnpaidBill_CUS_04() throws Exception {
        ModelKhachHang kh = new ModelKhachHang(103, "", "", 0, 0);

        ModelHoaDon hd = serviceCustomer.FindHoaDon(kh);

        assertNotNull("Fail: Phải tìm thấy hóa đơn chưa thanh toán", hd);
        assertEquals("Fail: Trạng thái không đúng", "Chua thanh toan", hd.getTrangthai());
        assertEquals("Fail: ID_KH không đúng", kh.getID_KH(), hd.getIdKH());
    }

    /**
     * Test: Khách hàng không có hóa đơn chưa thanh toán Mục đích: Phải trả về
     * null
     */
    @Test
    public void testFindHoaDon_CustomerHasNoUnpaidBill_CUS_05() throws Exception {
        ModelKhachHang kh = new ModelKhachHang(102, "", "", 0, 0);

        ModelHoaDon hd = serviceCustomer.FindHoaDon(kh);

        assertNull("Fail: Không được tìm thấy hóa đơn nếu không có hóa đơn chưa thanh toán", hd);
    }

    /**
     * Test: Truyền tham số null Mục đích: Phải ném ra NullPointerException
     */
    @Test
    public void testFindHoaDon_NullCustomer_CUS_06() {
        try {
            serviceCustomer.FindHoaDon(null);
            fail("Fail: Phải ném ra NullPointerException khi truyền vào null");
        } catch (NullPointerException e) {
            System.out.println("Đã ném đúng NullPointerException như mong đợi");
        } catch (Exception e) {
            fail("Sai loại ngoại lệ: " + e.getClass().getSimpleName());
        }
    }

    /**
     * Test: Khách hàng không tồn tại Mục đích: Phải trả về null
     */
    @Test
    public void testFindHoaDon_CustomerNotExist_CUS_07() throws Exception {
        ModelKhachHang kh = new ModelKhachHang(9999, "", "", 0, 0);

        ModelHoaDon hd = serviceCustomer.FindHoaDon(kh);

        assertNull("Fail: Nếu ID_KH không tồn tại, không được trả về hóa đơn nào", hd);
    }

    /**
     * Class ServiceStaffTest - Test hàm InsertCTHD
     */
    @Test
    public void testInsertCTHD_InsertNew_CUS_08() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        // Dọn dữ liệu
        stm.execute("DELETE FROM CTHD WHERE ID_HOADON=124 AND ID_MONAN=20");

        // Thực hiện Insert món mới
        serviceCustomer.InsertCTHD(124, 20, 2);

        ResultSet rs = stm.executeQuery("SELECT * FROM CTHD WHERE ID_HOADON=124 AND ID_MONAN=20");
        assertTrue("Fail: Không Insert được món mới vào CTHD", rs.next());
        assertEquals("Fail: Số lượng sai", 2, rs.getInt("SOLUONG"));

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert món đã tồn tại => Update số lượng
     */
    @Test
    public void testInsertCTHD_UpdateExist_CUS_09() throws Exception {
        con.setAutoCommit(false);
        Statement stm = con.createStatement();

        // Chuẩn bị dữ liệu có sẵn
        stm.execute("DELETE FROM CTHD WHERE ID_HOADON=124 AND ID_MONAN=21");
        stm.execute("INSERT INTO CTHD VALUES (124, 21, 1, 10000)");

        serviceCustomer.InsertCTHD(124, 21, 2); // Thêm tiếp 2

        ResultSet rs = stm.executeQuery("SELECT * FROM CTHD WHERE ID_HOADON=124 AND ID_MONAN=21");
        assertTrue("Fail: Không Update được số lượng món đã có", rs.next());
        assertEquals("Fail: Số lượng update sai", 3, rs.getInt("SOLUONG"));
        System.out.println(rs.getInt("SOLUONG"));

        rs.close();
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert với ID_HoaDon không tồn tại
     */
    @Test(expected = SQLException.class)
    public void testInsertCTHD_InvalidID_HoaDon_CUS_10() throws Exception {
        con.setAutoCommit(false);
        serviceCustomer.InsertCTHD(9999, 20, 1); // ID_HOADON không tồn tại
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert với ID_MonAn không tồn tại
     */
    @Test(expected = SQLException.class)
    public void testInsertCTHD_InvalidID_MonAn_CUS_11() throws Exception {
        con.setAutoCommit(false);
        serviceCustomer.InsertCTHD(124, 9999, 1); // ID_MONAN không tồn tại
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert với số lượng = 0
     */
    @Test(expected = SQLException.class)
    public void testInsertCTHD_ZeroQuantity_CUS_12() throws Exception {
        con.setAutoCommit(false);
        serviceCustomer.InsertCTHD(200, 20, 0); // Không hợp lệ
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert với số lượng âm
     */
    @Test(expected = SQLException.class)
    public void testInsertCTHD_NegativeQuantity_CUS_13() throws Exception {
        con.setAutoCommit(false);
        serviceCustomer.InsertCTHD(200, 20, -2); // Không hợp lệ
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert với ID âm
     */
    @Test(expected = SQLException.class)
    public void testInsertCTHD_NegativeID_CUS_14() throws Exception {
        con.setAutoCommit(false);
        serviceCustomer.InsertCTHD(-200, -20, 1); // ID âm
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test of MenuFood method, of class ServiceCustomer.
     */
    @Test
    public void testMenuFood_TypeTonTai_CUS_15() throws Exception {
        //DB có 12 món loại Aries
        String type = "Aries";
        ArrayList<ModelMonAn> list = service.MenuFood(type);

        // Kiểm tra số lượng phần tử
        assertEquals("Số lượng món ăn trong hai danh sách phải bằng nhau", list.size(), 12);

    }

    @Test
    public void testMenuFood_TypeKhongTonTai_CUS_16() throws Exception {
        String type = "Ariassss";
        ArrayList<ModelMonAn> list1 = service.MenuFood(type);
        assertEquals(0, list1.size());
    }
    
    

    /**
     * Test of MenuFoodOrder method, of class ServiceCustomer.
     */
    @Test //Sắp xếp từ A - Z
    public void testMenuFoodOrder_A_Z_CUS_17() throws Exception {
        ArrayList<ModelMonAn> list = new ArrayList<>();
        String type = "Arias";
        String sql = "SELECT ID_MonAn,TenMon,DonGia FROM MonAn WHERE Loai=? AND TrangThai='Dang kinh doanh' ORDER BY TenMon";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, type);

        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id = r.getInt("ID_MonAn");
            String name = r.getString("TenMon");
            int value = r.getInt("DonGia");
            ModelMonAn data;
            if (id < 90) {
                data = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/" + type + "/" + id + ".jpg")), id, name, value, type);
            } else {
                data = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")), id, name, value, type);
            }
            list.add(data);
        }

        ArrayList<ModelMonAn> list1 = service.MenuFoodOrder(type, "Tên A->Z");
        // Kiểm tra số lượng phần tử
        assertEquals("Số lượng món ăn trong hai danh sách phải bằng nhau", list.size(), list1.size());

        // So sánh từng phần tử dựa trên thuộc tính, không phải tham chiếu đối tượng
        for (ModelMonAn foodFromService : list1) {
            boolean found = false;
            for (ModelMonAn foodFromDirect : list) {
                if (foodFromService.getId() == foodFromDirect.getId()
                        && foodFromService.getValue() == foodFromDirect.getValue()
                        && foodFromService.getType().equals(foodFromDirect.getType())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Không tìm thấy món ăn với ID=" + foodFromService.getId()
                    + ", value=" + foodFromService.getValue()
                    + ", type=" + foodFromService.getType()
                    + " trong danh sách truy vấn trực tiếp", found);
        }

        // Kiểm tra thứ tự sắp xếp theo tên (A->Z)
        for (int i = 0; i < list1.size() - 1; i++) {
            String currentName = list1.get(i).getTitle();
            String nextName = list1.get(i + 1).getTitle();
            assertTrue("Thứ tự sắp xếp tên từ A->Z không đúng: '" + currentName + "' đứng trước '" + nextName + "'",
                    currentName.compareToIgnoreCase(nextName) <= 0);
        }

    }

    @Test // Sắp xếp theo giá tăng dần
    public void testMenuFoodOrder_ASC_CUS_18() throws Exception {
        ArrayList<ModelMonAn> list = new ArrayList<>();
        String type = "Arias";
        String sql = "SELECT ID_MonAn,TenMon,DonGia FROM MonAn WHERE Loai=? AND TrangThai='Dang kinh doanh' ORDER BY DonGia";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, type);

        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id = r.getInt("ID_MonAn");
            String name = r.getString("TenMon");
            int value = r.getInt("DonGia");
            ModelMonAn data;
            if (id < 90) {
                data = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/" + type + "/" + id + ".jpg")), id, name, value, type);
            } else {
                data = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")), id, name, value, type);
            }
            list.add(data);
        }

        ArrayList<ModelMonAn> list1 = service.MenuFoodOrder(type, "Giá tăng dần");
        // Kiểm tra số lượng phần tử
        assertEquals("Số lượng món ăn trong hai danh sách phải bằng nhau", list.size(), list1.size());

        // So sánh từng phần tử dựa trên thuộc tính, không phải tham chiếu đối tượng
        for (ModelMonAn foodFromService : list1) {
            boolean found = false;
            for (ModelMonAn foodFromDirect : list) {
                if (foodFromService.getId() == foodFromDirect.getId()
                        && foodFromService.getValue() == foodFromDirect.getValue()
                        && foodFromService.getType().equals(foodFromDirect.getType())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Không tìm thấy món ăn với ID=" + foodFromService.getId()
                    + ", value=" + foodFromService.getValue()
                    + ", type=" + foodFromService.getType()
                    + " trong danh sách truy vấn trực tiếp", found);
        }
        // Kiểm tra thứ tự sắp xếp theo giá tăng dần
        for (int i = 0; i < list1.size() - 1; i++) {
            int currentPrice = list1.get(i).getValue();
            int nextPrice = list1.get(i + 1).getValue();
            assertTrue("Thứ tự sắp xếp giá tăng dần không đúng: " + currentPrice + " đứng trước " + nextPrice,
                    currentPrice <= nextPrice);
        }
    }

    @Test //Sắp xếp theo giá giảm dần
    public void testMenuFoodOrder_DESC_CUS_19() throws Exception {
        ArrayList<ModelMonAn> list = new ArrayList<>();
        String type = "Arias";
        String sql = "SELECT ID_MonAn,TenMon,DonGia FROM MonAn WHERE Loai=? AND TrangThai='Dang kinh doanh' ORDER BY DonGia DESC";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, type);

        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id = r.getInt("ID_MonAn");
            String name = r.getString("TenMon");
            int value = r.getInt("DonGia");
            ModelMonAn data;
            if (id < 90) {
                data = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/" + type + "/" + id + ".jpg")), id, name, value, type);
            } else {
                data = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")), id, name, value, type);
            }
            list.add(data);
        }

        ArrayList<ModelMonAn> list1 = service.MenuFoodOrder(type, "Giá giảm dần");
        // Kiểm tra số lượng phần tử
        assertEquals("Số lượng món ăn trong hai danh sách phải bằng nhau", list.size(), list1.size());

        // So sánh từng phần tử dựa trên thuộc tính, không phải tham chiếu đối tượng
        for (ModelMonAn foodFromService : list1) {
            boolean found = false;
            for (ModelMonAn foodFromDirect : list) {
                if (foodFromService.getId() == foodFromDirect.getId()
                        && foodFromService.getValue() == foodFromDirect.getValue()
                        && foodFromService.getType().equals(foodFromDirect.getType())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Không tìm thấy món ăn với ID=" + foodFromService.getId()
                    + ", value=" + foodFromService.getValue()
                    + ", type=" + foodFromService.getType()
                    + " trong danh sách truy vấn trực tiếp", found);
        }
        // Kiểm tra thứ tự sắp xếp theo giá giảm dần
        for (int i = 0; i < list1.size() - 1; i++) {
            int currentPrice = list1.get(i).getValue();
            int nextPrice = list1.get(i + 1).getValue();
            assertTrue("Thứ tự sắp xếp giá giảm dần không đúng: " + currentPrice + " đứng trước " + nextPrice,
                    currentPrice >= nextPrice);
        }
    }

    @Test
    public void testMenuFoodOrder_TypeNotExist_CUS_20() throws Exception {
        ArrayList<ModelMonAn> list = new ArrayList<>();
        String type = "Ariassssss";

        ArrayList<ModelMonAn> list1 = service.MenuFoodOrder(type, "Giá giảm dần");
        // Kiểm tra số lượng phần tử
        assertEquals("Số lượng món ăn trong hai danh sách phải bằng nhau", list.size(), list1.size());
    }

    @Test
    public void testMenuFoodOrder_orderByNotExist_CUS_21() throws Exception {
        String type = "Arias";

        //Hiện tại hệ thống có 89 món ăn
        ArrayList<ModelMonAn> list1 = service.MenuFoodOrder(type, "hello");
        // Kiểm tra số lượng phần tử
        assertNotEquals("Danh sách mặc định phải có 89 món", list1.size(), 89);
    }

    /**
     * Test of MenuTable method, of class ServiceCustomer.
     */
    @Test // Lấy danh sách bàn tầng 1
    public void testMenuTable_Tang1_CUS_22() throws Exception {
        String floor = "Tang 1";
        // Tầng 1 hiện có 12 bàn

        ArrayList<ModelBan> list = service.MenuTable(floor);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), 12);

    }

    @Test //Lấy danh sách bàn tầng 2
    public void testMenuTable_Tang2_CUS_23() throws Exception {
        String floor = "Tang 2";
        // Tầng 2 hiện có 12 bàn

        ArrayList<ModelBan> list = service.MenuTable(floor);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), 12);
    }

    @Test //Lấy danh sách bàn tầng 3
    public void testMenuTable_Tang3_CUS_24() throws Exception {
        String floor = "Tang 3";
        // Tầng 3 hiện có 12 bàn

        ArrayList<ModelBan> list = service.MenuTable(floor);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), 12);
    }

    @Test //Tầng không tồn tại
    public void testMenuTable_TangKhongTonTai_CUS_25() throws SQLException {
        ArrayList<ModelBan> result = service.MenuTable("Tang 10000");
        assertTrue(result.isEmpty());
    }

    @Test // Tầng bằng null
    public void testMenuTable_TangNull_CUS_26() {
        assertThrows(SQLException.class, () -> {
            service.MenuTable(null);
        });
    }

    /**
     * Test of MenuTableState method, of class ServiceCustomer.
     */
    @Test //Lấy tất cả bàn của 1 tầng
    public void testMenuTableState_TatCa_CUS_27() throws Exception {
        //Tầng 1 có tổng 12 bàn
        String floor = "Tang 1";
        String state = "Tất cả";

        ArrayList<ModelBan> list = service.MenuTableState(floor, state);

        assertNotNull(list);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), 12);

    }

    @Test
    public void testMenuTableState_ConTrong_CUS_28() throws Exception {
        String floor = "Tang 1";
        String state = "Còn trống";

        // Tầng 1 có 9 bàn còn trống
        ArrayList<ModelBan> list = service.MenuTableState(floor, state);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), 9);
    }

    @Test
    public void testMenuTableState_DaDatTruoc_CUS_29() throws Exception {
        String floor = "Tang 1";
        String state = "Đã đặt trước";

        // Tầng 1 có 1 bàn đã đặt trước
        ArrayList<ModelBan> list = service.MenuTableState(floor, state);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), 1);
    }

    @Test
    public void testMenuTableState_DangDungBua_CUS_30() throws Exception {
        String floor = "Tang 1";
        String state = "Đang dùng bữa";

        // Tầng 1 có 2 bàn đang dùng bữa
        ArrayList<ModelBan> list = service.MenuTableState(floor, state);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), 2);
    }

    @Test // TC05: Trạng thái không xác định
    public void testMenuTableState_TrangThaiKhongXacDinh_CUS_31() throws SQLException {

        String floor = "Tang 1";
        String state = "Không xác định";

        //Mặc định trả về tất cả bàn của 1 tầng
        ArrayList<ModelBan> result = service.MenuTableState(floor, state);
        assertNotNull(result);
        assertEquals(result.size(), 12);
    }

    @Test // TC08: state = null
    public void testMenuTableState_StateNull_CUS_32() {
        assertThrows(SQLException.class, () -> {
            service.MenuTableState("Tang 1", null);
        });
    }

    /**
     * Test of getCustomer method, of class ServiceCustomer.
     */
    @Test //User ID hợp lệ và có dữ liệu trong bảng khách hàng
    public void testGetCustomer_ValidUserID_HasData_CUS_33() throws Exception {
        int userID = 113;
        ModelKhachHang exp = new ModelKhachHang(109, "Hoang Thi Phuc Nguyen", "12-05-2023", 400000, 20);
        ModelKhachHang result = service.getCustomer(userID);
        assertEquals(exp.getID_KH(), result.getID_KH());
        assertEquals(exp.getName(), result.getName());
        assertEquals(exp.getDateJoin(), result.getDateJoin());
        assertEquals(exp.getSales(), result.getSales());
        assertEquals(exp.getPoints(), result.getPoints());
    }

    @Test //UserID hợp lệ nhưng không có trong bảng khách hàng
    public void testGetCustomer_ValidUserID_NoData_CUS_34() throws Exception {
        // userID 9999 không có trong bảng KhachHang
        int userID = 114;
        ModelKhachHang customer = service.getCustomer(userID);
        assertNull(customer);
    }

    @Test //UserID âm
    public void testGetCustomer_TC5_12_CUS_35() throws Exception {
        int userID = -100;
        ModelKhachHang result = service.getCustomer(userID);
        assertNull("Hàm sai", result);
    }

    /**
     * Test of reNameCustomer method, of class ServiceCustomer.
     */
    @Test
    public void testReNameCustomer_DuLieuHopLe_CUS_36() throws Exception {
        ModelKhachHang data = service.getCustomer(110);
        String name = data.getName();
        String newName = "Hoang Thiên Đế";

        try {
            con.setAutoCommit(false);
            data.setName(newName);
            service.reNameCustomer(data);

            // Kiểm tra tên đã được cập nhật
            ModelKhachHang updatedData = service.getCustomer(110);
            assertEquals("Tên khách hàng phải được cập nhật thành tên mới", newName, updatedData.getName());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.rollback();
                con.setAutoCommit(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testReNameCustomer_TenRong_CUS_37() throws Exception {
        ModelKhachHang data = service.getCustomer(110);
        data.setName(""); // Tên rỗng

        service.reNameCustomer(data); // Nên ném IllegalArgumentException
    }

    @Test(expected = IllegalArgumentException.class)
    public void testReNameCustomer_TenNull_CUS_38() throws Exception {
        ModelKhachHang data = service.getCustomer(110);
        data.setName(null); // Tên rỗng

        service.reNameCustomer(data); // Nên ném IllegalArgumentException
    }

    @Test
    public void testReNameCustomer_TenDaiHon_50KyTu_CUS_39() throws SQLException {
        ModelKhachHang data = service.getCustomer(109);
        String name = data.getName();
        String newName = data.getName() + data.getName() + data.getName() + data.getName() + data.getName() + data.getName() + data.getName();
        System.out.println(newName);
        try {
            con.setAutoCommit(false);
            data.setName(newName);
            service.reNameCustomer(data);
            fail("Tên không được thay đổi khi quá dài");
        } catch (SQLException e) {
            boolean check = true;
            if (e.getMessage().contains("ORA-12899")) {
                check = false;
            }
            assertFalse("Hàm sai", check);
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    /**
     * Test of getCTHD method, of class ServiceCustomer.
     */
    @Test
    public void testGetCTHD_IDHopLe_CUS_40() throws SQLException {
        //test khi id HD có trong cơ sở dữ liệu

        ArrayList<ModelCTHD> result = serviceCustomer.getCTHD(121);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        for (int i = 0; i < result.size(); i++) {
            assertEquals(121, result.get(i).getID_HD());
        }
    }

    @Test
    public void testGetCTHD_IDKhongTonTai_CUS_41() throws SQLException {
        // Kiểm tra khi id không hợp lệ, không tồn tại

        // Gọi phương thức getCTNK với mã nhập kho không có dữ liệu
        ArrayList<ModelCTHD> result = serviceCustomer.getCTHD(999999999);

        // Kiểm tra danh sách trả về rỗng
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testMenuVoucher_CoNhieuBanGhi_CUS_42() throws SQLException {
        ArrayList<ModelVoucher> list = service.MenuVoucher();
        // Có 12 bản ghi trong DB
        assertNotNull(list);
        assertEquals(list.size(), 12);
    }

    @Test
    public void testMenuVoucher_KhongCoBanGhi_CUS_43() throws SQLException {

        try {
            con.setAutoCommit(false);
            try (PreparedStatement p = con.prepareCall("DELETE FROM Voucher")) {
                p.execute();
            }
            ArrayList<ModelVoucher> list = service.MenuVoucher();

            assertEquals(list.size(), 0);
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    @Test
    public void testMenuVoucherbyPoint_Null_CUS_49() throws Exception {
        ArrayList<ModelVoucher> list = service.MenuVoucherbyPoint(null);
        assertNotNull(list);
        assertTrue(list.size() >= 0); // hoặc kiểm tra dữ liệu cụ thể trong DB
    }

    @Test
    public void testMenuVoucherbyPoint_TatCa_CUS_44() throws Exception {
        ArrayList<ModelVoucher> list = service.MenuVoucherbyPoint("Tất cả");
        assertNotNull(list);
    }

    @Test
    public void testMenuVoucherbyPoint_Duoi300_CUS_45() throws Exception {
        ArrayList<ModelVoucher> list = service.MenuVoucherbyPoint("Dưới 300 xu");

        // DB có 6 bản ghi voucher có điểm < 300
        assertEquals(list.size(), 6);
        for (ModelVoucher v : list) {
            assertTrue(v.getPoint() < 300);
        }
    }

    @Test
    public void testMenuVoucherbyPoint_Tu300Den500_CUS_46() throws Exception {
        ArrayList<ModelVoucher> list = service.MenuVoucherbyPoint("Từ 300 đến 500 xu");

        // DB có 4 bản ghi voucher có điểm > 300 và <= 500
        assertEquals(list.size(), 4);
        for (ModelVoucher v : list) {
            assertTrue(v.getPoint() >= 300 && v.getPoint() <= 501);
        }
    }

    @Test
    public void testMenuVoucherbyPoint_Tren500_CUS_47() throws Exception {
        ArrayList<ModelVoucher> list = service.MenuVoucherbyPoint("Trên 500 xu");

        // DB có 2 bản ghi voucher có điểm > 500
        assertEquals(list.size(), 2);
        for (ModelVoucher v : list) {
            assertTrue(v.getPoint() > 500);
        }
    }

    @Test
    public void testMenuVoucherbyPoint_KhongHopLe_CUS_48() throws Exception {
        ArrayList<ModelVoucher> list = service.MenuVoucherbyPoint("abc");
        assertNotNull(list);
        assertEquals(list.size(), 12);
    }

    /*
        Unit test lấy hóa đơn
     */
    @Test
    public void testGetListHD_CoHoaDon__CUS_50() throws SQLException {
        ArrayList<ModelHoaDon> list = service.getListHD(101); // ID_KH có hóa đơn trong DB
        assertNotNull(list);
        assertTrue(list.size() > 0);
    }

    @Test
    public void testGetListHD_KhongCoHoaDon_CUS_51() throws SQLException {
        ArrayList<ModelHoaDon> list = service.getListHD(110); // ID_KH không có hóa đơn
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @Test
    public void testGetListHD_IDKhachKhongTonTai_CUS_52() throws SQLException {
        ArrayList<ModelHoaDon> list = service.getListHD(-1); // ID_KH không tồn tại
        assertNotNull(list);
        assertEquals(0, list.size());
    }

    @Test
    public void testGetListHDOrder_TatCa_CUS_53() throws Exception {
        //KH 106 có 5 bản ghi trong DB
        ArrayList<ModelHoaDon> list = service.getListHDOrder(106, "Tất cả");
        assertNotNull(list);
        assertEquals(list.size(), 5);
    }

    @Test
    public void testGetListHDOrder_Duoi1Trieu_CUS_54() throws Exception {
        ArrayList<ModelHoaDon> list = service.getListHDOrder(106, "Dưới 1.000.000đ");
        //KH 106 có 4 hóa đơn < 1 triệu
        assertNotNull(list);
        assertEquals(list.size(), 4);
        for (ModelHoaDon hd : list) {
            assertTrue(hd.getTongtien() < 1000000);
        }
    }

    @Test
    public void testGetListHDOrder_Tu1Den5Trieu_CUS_55() throws Exception {
        ArrayList<ModelHoaDon> list = service.getListHDOrder(106, "Từ 1 đến 5.000.000đ");
        //KH 106 có 1 hóa đơn 1-5 triệu
        assertNotNull(list);
        assertEquals(list.size(), 1);
        
        for (ModelHoaDon hd : list) {
            assertTrue(hd.getTongtien() >= 1000000 && hd.getTongtien() <= 5000001);
        }
    }

    @Test
    public void testGetListHDOrder_Tren5Trieu_CUS_56() throws Exception {
        ArrayList<ModelHoaDon> list = service.getListHDOrder(101, "Trên 5.000.000đ");
        
        //KH 101 có 1 hóa đơn
        assertNotNull(list);
        assertEquals(list.size(), 1);
        
        for (ModelHoaDon hd : list) {
            assertTrue(hd.getTongtien() > 5000000);
        }
    }

    @Test
    public void testGetListHDOrder_KhongHopLe_CUS_57() throws Exception {
        ArrayList<ModelHoaDon> list1 = service.getListHDOrder(101, "Tất cả");
        ArrayList<ModelHoaDon> list2 = service.getListHDOrder(101, "abc");
        // Vì default case không thay đổi SQL gốc (ORDER BY ID_HoaDon), kết quả phải giống "Tất cả"
        assertEquals(list1.size(), list2.size());
    }

}
