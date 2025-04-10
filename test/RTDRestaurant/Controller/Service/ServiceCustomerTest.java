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
import RTDRestaurant.Controller.Connection.DatabaseConnection;
import RTDRestaurant.Model.ModelBan;
import RTDRestaurant.Model.ModelCTHD;
import RTDRestaurant.Model.ModelHoaDon;
import RTDRestaurant.Model.ModelKhachHang;
import RTDRestaurant.Model.ModelMonAn;
import RTDRestaurant.Model.ModelVoucher;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.ImageIcon;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import static org.junit.Assert.*;
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
    public void testInsertHoaDon_ValidData() {
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
    public void testInsertHoaDon_InvalidCustomer() {
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
    public void testInsertHoaDon_InvalidTable() {
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
    // Test FindHoaDon - Tìm hóa đơn theo khách hàng
    //------------------------------------------------------------
    /**
     * Test: Khách hàng có hóa đơn chưa thanh toán Mục đích: Phải tìm thấy đúng
     * hóa đơn chưa thanh toán
     */
    @Test
    public void testFindHoaDon_CustomerHasUnpaidBill() throws Exception {
        ModelKhachHang kh = new ModelKhachHang(110, "", "", 0, 0);

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
    public void testFindHoaDon_CustomerHasNoUnpaidBill() throws Exception {
        ModelKhachHang kh = new ModelKhachHang(100, "", "", 0, 0);

        ModelHoaDon hd = serviceCustomer.FindHoaDon(kh);

        assertNull("Fail: Không được tìm thấy hóa đơn nếu không có hóa đơn chưa thanh toán", hd);
    }

    /**
     * Test: Truyền tham số null Mục đích: Phải ném ra NullPointerException
     */
    @Test
    public void testFindHoaDon_NullCustomer() {
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
    public void testFindHoaDon_CustomerNotExist() throws Exception {
        ModelKhachHang kh = new ModelKhachHang(9999, "", "", 0, 0);

        ModelHoaDon hd = serviceCustomer.FindHoaDon(kh);

        assertNull("Fail: Nếu ID_KH không tồn tại, không được trả về hóa đơn nào", hd);
    }

    /**
     * Class ServiceStaffTest - Test hàm InsertCTHD
     */
    @Test
    public void testInsertCTHD_InsertNew() throws Exception {
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
    public void testInsertCTHD_UpdateExist() throws Exception {
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
    public void testInsertCTHD_InvalidID_HoaDon() throws Exception {
        con.setAutoCommit(false);
        serviceCustomer.InsertCTHD(9999, 20, 1); // ID_HOADON không tồn tại
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert với ID_MonAn không tồn tại
     */
    @Test(expected = SQLException.class)
    public void testInsertCTHD_InvalidID_MonAn() throws Exception {
        con.setAutoCommit(false);
        serviceCustomer.InsertCTHD(124, 9999, 1); // ID_MONAN không tồn tại
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert với số lượng = 0
     */
    @Test(expected = SQLException.class)
    public void testInsertCTHD_ZeroQuantity() throws Exception {
        con.setAutoCommit(false);
        serviceCustomer.InsertCTHD(200, 20, 0); // Không hợp lệ
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert với số lượng âm
     */
    @Test(expected = SQLException.class)
    public void testInsertCTHD_NegativeQuantity() throws Exception {
        con.setAutoCommit(false);
        serviceCustomer.InsertCTHD(200, 20, -2); // Không hợp lệ
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test: Insert với ID âm
     */
    @Test(expected = SQLException.class)
    public void testInsertCTHD_NegativeID() throws Exception {
        con.setAutoCommit(false);
        serviceCustomer.InsertCTHD(-200, -20, 1); // ID âm
        con.rollback();
        con.setAutoCommit(true);
    }

    /**
     * Test of MenuFood method, of class ServiceCustomer.
     */
    @Test
    public void testMenuFood_TC6_11() throws Exception {
        String type = "Arias";
        ArrayList<ModelMonAn> list = new ArrayList<>();
        String sql = "SELECT ID_MonAn,TenMon,DonGia FROM MonAn WHERE Loai=? AND TrangThai='Dang kinh doanh'";
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

        ArrayList<ModelMonAn> list1 = service.MenuFood(type);

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
    }

    @Test
    public void testMenuFood_TC6_12() throws Exception {
        String type = "Ariassss";
        ArrayList<ModelMonAn> list1 = service.MenuFood(type);
        assertEquals(0, list1.size());
    }

    /**
     * Test of MenuFoodOrder method, of class ServiceCustomer.
     */
    @Test
    public void testMenuFoodOrder_TC6_21() throws Exception {
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

    @Test
    public void testMenuFoodOrder_TC6_22() throws Exception {
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

    @Test
    public void testMenuFoodOrder_TC6_23() throws Exception {
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

    /**
     * Test of MenuTable method, of class ServiceCustomer.
     */
    @Test
    public void testMenuTable_TC5_21() throws Exception {
        String floor = "Tang 1";
        ArrayList<ModelBan> list = new ArrayList<>();
        String sql = "SELECT ID_Ban,TenBan,Trangthai FROM Ban WHERE Vitri=?";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, floor);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id = r.getInt("ID_Ban");
            String name = r.getString("TenBan");
            String status = r.getString("Trangthai");
            ModelBan data = new ModelBan(id, name, status);
            list.add(data);
        }
        System.out.println(list);
        ArrayList<ModelBan> list1 = service.MenuTable(floor);
        System.out.println(list1);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), list1.size());

        // So sánh từng phần tử dựa trên thuộc tính, không phải tham chiếu đối tượng
        for (ModelBan tableFromService : list1) {
            boolean found = false;
            for (ModelBan tableFromDirect : list) {
                if (tableFromService.getID() == tableFromDirect.getID()
                        && tableFromService.getName().equals(tableFromDirect.getName())
                        && tableFromService.getStatus().equals(tableFromDirect.getStatus())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Không tìm thấy bàn với ID=" + tableFromService.getID()
                    + ", name=" + tableFromService.getName()
                    + ", status=" + tableFromService.getStatus()
                    + " trong danh sách truy vấn trực tiếp", found);
        }

    }

    @Test
    public void testMenuTable_TC5_22() throws Exception {
        String floor = "Tang 2";
        ArrayList<ModelBan> list = new ArrayList<>();
        String sql = "SELECT ID_Ban,TenBan,Trangthai FROM Ban WHERE Vitri=?";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, floor);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id = r.getInt("ID_Ban");
            String name = r.getString("TenBan");
            String status = r.getString("Trangthai");
            ModelBan data = new ModelBan(id, name, status);
            list.add(data);
        }

        ArrayList<ModelBan> list1 = service.MenuTable(floor);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), list1.size());

        // So sánh từng phần tử dựa trên thuộc tính, không phải tham chiếu đối tượng
        for (ModelBan tableFromService : list1) {
            boolean found = false;
            for (ModelBan tableFromDirect : list) {
                if (tableFromService.getID() == tableFromDirect.getID()
                        && tableFromService.getName().equals(tableFromDirect.getName())
                        && tableFromService.getStatus().equals(tableFromDirect.getStatus())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Không tìm thấy bàn với ID=" + tableFromService.getID()
                    + ", name=" + tableFromService.getName()
                    + ", status=" + tableFromService.getStatus()
                    + " trong danh sách truy vấn trực tiếp", found);
        }
    }

    @Test
    public void testMenuTable_TC5_23() throws Exception {
        String floor = "Tang 3";
        ArrayList<ModelBan> list = new ArrayList<>();
        String sql = "SELECT ID_Ban,TenBan,Trangthai FROM Ban WHERE Vitri=?";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, floor);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id = r.getInt("ID_Ban");
            String name = r.getString("TenBan");
            String status = r.getString("Trangthai");
            ModelBan data = new ModelBan(id, name, status);
            list.add(data);
        }

        ArrayList<ModelBan> list1 = service.MenuTable(floor);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), list1.size());

        // So sánh từng phần tử dựa trên thuộc tính, không phải tham chiếu đối tượng
        for (ModelBan tableFromService : list1) {
            boolean found = false;
            for (ModelBan tableFromDirect : list) {
                if (tableFromService.getID() == tableFromDirect.getID()
                        && tableFromService.getName().equals(tableFromDirect.getName())
                        && tableFromService.getStatus().equals(tableFromDirect.getStatus())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Không tìm thấy bàn với ID=" + tableFromService.getID()
                    + ", name=" + tableFromService.getName()
                    + ", status=" + tableFromService.getStatus()
                    + " trong danh sách truy vấn trực tiếp", found);
        }
    }

    /**
     * Test of MenuTableState method, of class ServiceCustomer.
     */
    @Test
    public void testMenuTableState_TC5_31() throws Exception {
        String floor = "Tang 1";
        String state = "Tất cả";
        ArrayList<ModelBan> list = service.MenuTable(floor);
        ArrayList<ModelBan> list1 = service.MenuTableState(floor, state);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), list1.size());

        // So sánh từng phần tử dựa trên thuộc tính, không phải tham chiếu đối tượng
        for (ModelBan tableFromService : list1) {
            boolean found = false;
            for (ModelBan tableFromDirect : list) {
                if (tableFromService.getID() == tableFromDirect.getID()
                        && tableFromService.getName().equals(tableFromDirect.getName())
                        && tableFromService.getStatus().equals(tableFromDirect.getStatus())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Không tìm thấy bàn với ID=" + tableFromService.getID()
                    + ", name=" + tableFromService.getName()
                    + ", status=" + tableFromService.getStatus()
                    + " trong danh sách truy vấn trực tiếp", found);
        }
    }

    @Test
    public void testMenuTableState_TC5_32() throws Exception {
        String floor="Tang 1";
        String state="Còn trống";
        
        ArrayList<ModelBan> list = new ArrayList<>();
        String sql="SELECT ID_Ban,TenBan,Trangthai FROM Ban WHERE Vitri=? AND Trangthai='Con trong'";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, floor);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id = r.getInt("ID_Ban");
            String name = r.getString("TenBan");
            String status = r.getString("Trangthai");
            ModelBan data = new ModelBan(id, name, status);
            list.add(data);
        }
        
        ArrayList<ModelBan> list1=service.MenuTableState(floor, state);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), list1.size());

        // So sánh từng phần tử dựa trên thuộc tính, không phải tham chiếu đối tượng
        for (ModelBan tableFromService : list1) {
            boolean found = false;
            for (ModelBan tableFromDirect : list) {
                if (tableFromService.getID() == tableFromDirect.getID() &&
                    tableFromService.getName().equals(tableFromDirect.getName()) &&
                    tableFromService.getStatus().equals(tableFromDirect.getStatus())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Không tìm thấy bàn với ID=" + tableFromService.getID() + 
                      ", name=" + tableFromService.getName() + 
                      ", status=" + tableFromService.getStatus() + 
                      " trong danh sách truy vấn trực tiếp", found);
        }      
    }

    @Test
    public void testMenuTableState_TC5_33() throws Exception {
        String floor = "Tang 1";
        String state = "Đã đặt trước";

        ArrayList<ModelBan> list = new ArrayList<>();
        String sql = "SELECT ID_Ban,TenBan,Trangthai FROM Ban WHERE Vitri=? AND Trangthai='Da dat truoc'";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, floor);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id = r.getInt("ID_Ban");
            String name = r.getString("TenBan");
            String status = r.getString("Trangthai");
            ModelBan data = new ModelBan(id, name, status);
            list.add(data);
        }

        ArrayList<ModelBan> list1 = service.MenuTableState(floor, state);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), list1.size());

        // So sánh từng phần tử dựa trên thuộc tính, không phải tham chiếu đối tượng
        for (ModelBan tableFromService : list1) {
            boolean found = false;
            for (ModelBan tableFromDirect : list) {
                if (tableFromService.getID() == tableFromDirect.getID()
                        && tableFromService.getName().equals(tableFromDirect.getName())
                        && tableFromService.getStatus().equals(tableFromDirect.getStatus())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Không tìm thấy bàn với ID=" + tableFromService.getID()
                    + ", name=" + tableFromService.getName()
                    + ", status=" + tableFromService.getStatus()
                    + " trong danh sách truy vấn trực tiếp", found);
        }
    }

    @Test
    public void testMenuTableState_TC5_34() throws Exception {
        String floor = "Tang 1";
        String state = "Đang dùng bữa";

        ArrayList<ModelBan> list = new ArrayList<>();
        String sql = "SELECT ID_Ban,TenBan,Trangthai FROM Ban WHERE Vitri=? AND Trangthai='Dang dung bua'";
        PreparedStatement p = con.prepareStatement(sql);
        p.setString(1, floor);
        ResultSet r = p.executeQuery();
        while (r.next()) {
            int id = r.getInt("ID_Ban");
            String name = r.getString("TenBan");
            String status = r.getString("Trangthai");
            ModelBan data = new ModelBan(id, name, status);
            list.add(data);
        }

        ArrayList<ModelBan> list1 = service.MenuTableState(floor, state);
        // So sánh kích thước
        assertEquals("Số lượng bàn trong hai danh sách phải bằng nhau", list.size(), list1.size());

        // So sánh từng phần tử dựa trên thuộc tính, không phải tham chiếu đối tượng
        for (ModelBan tableFromService : list1) {
            boolean found = false;
            for (ModelBan tableFromDirect : list) {
                if (tableFromService.getID() == tableFromDirect.getID()
                        && tableFromService.getName().equals(tableFromDirect.getName())
                        && tableFromService.getStatus().equals(tableFromDirect.getStatus())) {
                    found = true;
                    break;
                }
            }
            assertTrue("Không tìm thấy bàn với ID=" + tableFromService.getID()
                    + ", name=" + tableFromService.getName()
                    + ", status=" + tableFromService.getStatus()
                    + " trong danh sách truy vấn trực tiếp", found);
        }
    }

    /**
     * Test of getCustomer method, of class ServiceCustomer.
     */
    @Test
    public void testGetCustomer_TC5_11() throws Exception {
        int userID = 113;
        ModelKhachHang exp = new ModelKhachHang(109, "Hoang Thi Phuc Nguyen", "12-05-2023", 400000, 20);
        ModelKhachHang result = service.getCustomer(userID);
        assertEquals(exp.getID_KH(), result.getID_KH());
        assertEquals(exp.getName(), result.getName());
        assertEquals(exp.getDateJoin(), result.getDateJoin());
        assertEquals(exp.getSales(), result.getSales());
        assertEquals(exp.getPoints(), result.getPoints());
    }

    @Test
    public void testGetCustomer_TC5_12() throws Exception {
        int userID = -100;
        ModelKhachHang result = service.getCustomer(userID);
        assertNull("Hàm sai", result);
    }

    @Test
    public void testGetCustomer_TC5_13() throws Exception {
        int userID = 9999;
        ModelKhachHang result = service.getCustomer(userID);
        assertNull("Hàm sai", result);
    }

    /**
     * Test of reNameCustomer method, of class ServiceCustomer.
     */
    @Test
    public void testReNameCustomer_TC3_1() throws Exception {
        ModelKhachHang data = service.getCustomer(110);
        String name = data.getName();
        String newName = "Ngân 1234";

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

    @Test
    public void testReNameCustomer_TC3_2() throws Exception {
        ModelKhachHang data = service.getCustomer(110);
        String name = data.getName();
        String newName = "";

        try {
            con.setAutoCommit(false);
            data.setName(newName);
            service.reNameCustomer(data);

            ModelKhachHang updatedData = service.getCustomer(110);
            assertEquals("Tên mới không được rỗng", name, updatedData.getName());
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    @Test
    public void testReNameCustomer_TC3_3() throws Exception {
        ModelKhachHang data = service.getCustomer(110);
        String name = data.getName();
        String newName = data.getName();
        boolean test = true;
        try {
            con.setAutoCommit(false);
            data.setName(newName);
            service.reNameCustomer(data);
            test = false;
            assertTrue("Tên mới không được trùng với tên cũ", test);
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    @Test
    public void testReNameCustomer_TC3_4() throws SQLException {
        ModelKhachHang data = service.getCustomer(110);
        String name = data.getName();
        String newName = data.getName() + data.getName() + data.getName() + data.getName() + data.getName() + data.getName() + data.getName();
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
     * Test of InsertHoaDon method, of class ServiceCustomer.
     */
    @Test
    public void testInsertHoaDon_TC5_42() throws Exception {
        ModelBan table = new ModelBan();
        table.setID(100);
        ModelKhachHang customer = new ModelKhachHang(110, "Tran Thi Kim Ngan", "28-02-2025", 3824000, 9791);

        try {
            con.setAutoCommit(false);

            service.InsertHoaDon(table, customer);
            ModelHoaDon hoadon = service.FindHoaDon(customer);

            ModelBan table1 = new ModelBan();
            table1.setID(101);
            service.InsertHoaDon(table1, customer);

            ModelHoaDon hoadon1 = service.FindHoaDon(customer);
            assertEquals(hoadon.getIdBan(), hoadon1.getIdBan());
            assertEquals(customer.getID_KH(), hoadon1.getIdKH());
            assertEquals(hoadon.getTienMonAn(), hoadon1.getTienMonAn());
            assertEquals(hoadon.getTienGiam(), hoadon1.getTienGiam());
            assertEquals(hoadon.getTongtien(), hoadon1.getTongtien());
            assertEquals("Chua thanh toan", hoadon1.getTrangthai());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-YYYY");
            assertEquals(simpleDateFormat.format(new Date()), hoadon1.getNgayHD());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    @Test
    public void testInsertHoaDon_TC5_41() throws Exception {
        ModelBan table = new ModelBan();
        table.setID(100);
        ModelKhachHang customer = new ModelKhachHang(110, "Tran Thi Kim Ngan", "28-02-2025", 3824000, 9791);
        try {
            con.setAutoCommit(false);
            service.InsertHoaDon(table, customer);

            ModelHoaDon hoadon = service.FindHoaDon(customer);
            assertEquals(table.getID(), hoadon.getIdBan());
            assertEquals(customer.getID_KH(), hoadon.getIdKH());
            assertEquals(0, hoadon.getTienMonAn());
            assertEquals(0, hoadon.getTienGiam());
            assertEquals(0, hoadon.getTongtien());
            assertEquals("Chua thanh toan", hoadon.getTrangthai());
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-YYYY");
            assertEquals(simpleDateFormat.format(new Date()), hoadon.getNgayHD());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

    @Test
    public void testInsertHoaDon_TC5_43() throws Exception {
        ModelBan table = new ModelBan();
        table.setID(100);
        ServiceStaff s = new ServiceStaff();
        s.setTableReserve(100);

        try {
            con.setAutoCommit(false);

            ModelKhachHang customer1 = new ModelKhachHang(101, "Truong Tan Hieu", "10-05-2023", 0, 0);
            service.InsertHoaDon(table, customer1);

            ModelHoaDon hoadon1 = service.FindHoaDon(customer1);
            assertNull(hoadon1);
            if (hoadon1 != null) {
                assertNotEquals("Hàm sai do vẫn thêm hóa đơn mới cho bàn đang dùng bữa", table.getID(), hoadon1.getIdBan());
                assertNotEquals("Hàm sai do vẫn thêm hóa đơn mới cho bàn đang dùng bữa", customer1.getID_KH(), hoadon1.getIdKH());
                assertEquals("Hàm sai do vẫn thêm hóa đơn mới cho bàn đang dùng bữa", 0, hoadon1.getTienMonAn());
                assertEquals("Hàm sai do vẫn thêm hóa đơn mới cho bàn đang dùng bữa", 0, hoadon1.getTienGiam());
                assertEquals("Hàm sai do vẫn thêm hóa đơn mới cho bàn đang dùng bữa", 0, hoadon1.getTongtien());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-YYYY");
                assertEquals("Hàm sai do vẫn thêm hóa đơn mới cho bàn đang dùng bữa", simpleDateFormat.format(new Date()), hoadon1.getNgayHD());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            con.rollback();
            con.setAutoCommit(true);
        }
    }

   

    /**
     * Test of getCTHD method, of class ServiceCustomer.
     */
    @Test
    public void testGetCTNK() throws SQLException {
        //test khi id HD có trong cơ sở dữ liệu
                
        
        ArrayList<ModelCTHD> result = serviceCustomer.getCTHD(121);

        // Kiểm tra kết quả
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());

        // Kiểm tra chi tiết nhập kho 
        for( int i =0 ; i< result.size() ; i++){
            assertEquals(121, result.get(i).getID_HD());
        }
    }
    @Test
    public void testGetCTNKWhenNoResults() throws SQLException {
        // Kiểm tra khi id không hợp lệ, không tồn tại
        
        // Gọi phương thức getCTNK với mã nhập kho không có dữ liệu
        ArrayList<ModelCTHD> result = serviceCustomer.getCTHD(999);

        // Kiểm tra danh sách trả về rỗng
        assertNotNull(result);  
        assertTrue(result.isEmpty());
    }

    
    

}
