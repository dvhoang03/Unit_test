/*
 * Class ServiceAdminTest
 * Mục đích: Thực hiện kiểm thử (unit test) cho các hàm trong class ServiceAdmin
 * Cụ thể: Hàm getListHDIn(String txt) - Lấy danh sách hóa đơn theo tiêu chí truyền vào
 * 
 * Công cụ sử dụng:
 * - JUnit 4
 * - Java SQL Connection thật
 * - Database có dữ liệu thật
 * 
 * Quy ước message assert:
 * - Rõ ràng, dễ hiểu, mô tả chính xác ý nghĩa khi kiểm tra sai
 */
package RTDRestaurant.Controller.Service;


import RTDRestaurant.Controller.Connection.DatabaseConnection;
import RTDRestaurant.Model.ModelBan;
import RTDRestaurant.Model.ModelHoaDon;
import RTDRestaurant.Model.ModelKhachHang;
import RTDRestaurant.Model.ModelMonAn;
import RTDRestaurant.Model.ModelNhanVien;
import java.sql.*;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

import RTDRestaurant.Controller.Connection.DatabaseConnection;
import RTDRestaurant.Model.ModelHoaDon;
import RTDRestaurant.Model.ModelNhanVien;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class ServiceAdminTest {

    Connection con;
    ServiceAdmin serviceAdmin;

    private ServiceAdmin sa;
    private ServiceStaff ss;
    private ServiceCustomer sc;

    public ServiceAdminTest() {
    }

    /**
     * Hàm setUp() - Thiết lập kết nối DB và khởi tạo ServiceAdmin
     */
    @Before
    public void setUp() throws SQLException {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        dbConnection.connectToDatabase();
        con = dbConnection.getConnection();
        serviceAdmin = new ServiceAdmin();

        sa = new ServiceAdmin();
        ss = new ServiceStaff();
        sc = new ServiceCustomer();

    }

    /**
     * Test case: Truyền "Tất cả" -> Lấy toàn bộ hóa đơn Mong đợi: Danh sách
     * không null
     */
    @Test
    public void testGetListHDIn_TatCa() throws SQLException {
        ArrayList<ModelHoaDon> list = serviceAdmin.getListHDIn("Tất cả");

        assertNotNull("Fail: Danh sách hoá đơn không được null khi truyền 'Tất cả'", list);
        // Có thể kiểm tra thêm kích thước lớn hơn 0 nếu DB có sẵn dữ liệu
    }

    /**
     * Test case: Truyền "Hôm nay" -> Lấy các hóa đơn có ngày là hôm nay Mong
     * đợi: Danh sách không null và ngày các hóa đơn = hôm nay
     */
    @Test
    public void testGetListHDIn_HomNay() throws SQLException {
        ArrayList<ModelHoaDon> list = serviceAdmin.getListHDIn("Hôm nay");

        assertNotNull("Fail: Danh sách hoá đơn hôm nay không được null", list);

        for (ModelHoaDon hd : list) {
            assertTrue("Fail: Ngày hoá đơn phải đúng là hôm nay",
                    hd.getNgayHD().equals(java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
        }
    }

    /**
     * Test case: Truyền "Tháng này" -> Lấy các hóa đơn trong tháng hiện tại
     * Mong đợi: Danh sách không null
     */
    @Test
    public void testGetListHDIn_ThangNay() throws SQLException {
        ArrayList<ModelHoaDon> list = serviceAdmin.getListHDIn("Tháng này");

        assertNotNull("Fail: Danh sách hoá đơn tháng này không được null", list);
        // Có thể kiểm tra thêm tháng nếu muốn
    }

    /**
     * Test case: Truyền "Năm nay" -> Lấy các hóa đơn trong năm hiện tại Mong
     * đợi: Danh sách không null
     */
    @Test
    public void testGetListHDIn_NamNay() throws SQLException {
        ArrayList<ModelHoaDon> list = serviceAdmin.getListHDIn("Năm này");

        assertNotNull("Fail: Danh sách hoá đơn năm nay không được null", list);
        // Có thể kiểm tra thêm năm nếu muốn
    }

    //**
//    * Test case: Truyền giá trị không hợp lệ -> Mong đợi: Ném ra IllegalArgumentException
//    * Ghi chú: Vì code gốc chưa xử lý nên test này chủ động kiểm tra và ép fail nếu không có exception
//    */
    @Test
    public void testGetListHDIn_KhongHopLe() throws SQLException {
        try {
            // Truyền tham số không hợp lệ
            ArrayList<ModelHoaDon> list = serviceAdmin.getListHDIn("abc");

            // Nếu chạy được tới đây là sai kỳ vọng => Ép fail
            fail("Fail: Phải ném ra IllegalArgumentException khi truyền tham số không hợp lệ, nhưng hiện tại code không xử lý.");

        } catch (IllegalArgumentException e) {
            // Kiểm tra message ngoại lệ có đúng không
            assertEquals("Fail: Message ngoại lệ không đúng.",
                    "Giá trị truyền vào không hợp lệ! Chỉ cho phép: Tất cả, Hôm nay, Tháng này, Năm này.",
                    e.getMessage());
        } catch (Exception e) {
            // Nếu ném nhầm Exception khác cũng Fail luôn
            fail("Fail: Phải ném ra IllegalArgumentException, nhưng lại ném Exception loại khác: " + e.getClass().getSimpleName());
        }
    }

    @Test
    public void testGetListNV() throws Exception {
        //trong DB có 11 nhân viên
        ArrayList<ModelNhanVien> arr = sa.getListNV();
        assertEquals(11, arr.size());
    }

    @Test
    public void testGetNV_testChuan() throws Exception {
        //ID nhân viên 100 - 109
        ModelNhanVien nv = sa.getNV(100);
        assertNotNull(nv);
    }

    @Test
    public void testGetNV_IdBangKhong() throws Exception {
        //ID nhân viên 100 - 109
        ModelNhanVien nv = sa.getNV(0);
        assertNull(nv);
    }

    @Test
    public void testGetNV_IdKhongTonTai() throws Exception {
        //ID nhân viên 100 - 109
        ModelNhanVien nv = sa.getNV(999);
        assertNull(nv);
    }

    @Test
    public void testGetNV_IdSoAM() throws Exception {
        //ID nhân viên 100 - 109
        ModelNhanVien nv = sa.getNV(-99);
        assertNull(nv);
    }

    @Test
    public void testInsertNV_testChuan() throws Exception {
        ModelNhanVien nv = new ModelNhanVien(110, "Chan Be Du", "07-04-2025", "0123456789", "Bep", 100);

        try {
            con.setAutoCommit(false);
            sa.insertNV(nv);
            sa.getNV(111);

            assertNotNull(nv);
        } finally {
            try {
                con.rollback();
                con.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @Test
    public void testInsertNV_IDDaTonTai() throws Exception {
        ModelNhanVien nv = new ModelNhanVien(109, "Chan Be Du", "07-04-2025", "0123456789", "Bep", 100);

        try {
            con.setAutoCommit(false);
            sa.insertNV(nv);

            fail("ID đã tồn tại mà vẫn thêm được");
        } catch (SQLIntegrityConstraintViolationException e) {

            assertTrue(e.getMessage().contains("ORA"));
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
    public void testInsertNV_DuLieuKhongHopLe() throws Exception {
        //Khong có chức vụ "Lái xe"
        ModelNhanVien nv = new ModelNhanVien(110, "Chan Be Du", "07-04-2025", "0123456789", "Lai xe", 100);

        try {
            con.setAutoCommit(false);
            sa.insertNV(nv);

            fail("Thiếu trường tên nhân viên mà vẫn thêm được");
        } catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            assertTrue(true);
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
    public void testInsertNV_KhuyetTruongBatBuoc() throws Exception {
        ModelNhanVien nv = new ModelNhanVien(110, null, "07-04-2025", "0123456789", "Bep", 100);

        try {
            con.setAutoCommit(false);
            sa.insertNV(nv);

            fail("Thiếu trường tên nhân viên mà vẫn thêm được");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("ORA"));
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
    public void testInsertNV_SaiDinhDang() throws Exception {
        //định dạng ngày tháng đúng là dd-mm-yyyy
        ModelNhanVien nv = new ModelNhanVien(110, "Chan Be Du", "07/04/2025", "0123456789", "Bep", 100);

        try {
            con.setAutoCommit(false);
            sa.insertNV(nv);

            fail("Sai định dạng ngày tháng mà vẫn thêm được");
        } catch (SQLDataException e) {
            e.printStackTrace();
            assertTrue(true);
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
    public void testInsertNV_TenNhanVienQuaDai() throws Exception {
        //tên nhân viên hơn 50 ký tự
        ModelNhanVien nv = new ModelNhanVien(110,
                "Chan Be Du Chan Be Du Chan Be Du Chan Be Du Chan Be Du Chan Be Du Chan Be Du",
                "07-04-2025", "0123456789", "Bep", 100);

        try {
            con.setAutoCommit(false);
            sa.insertNV(nv);

            fail("Tên nhân viên hơn 50 ký mà vẫn thêm được");
        } catch (SQLException e) {
            e.printStackTrace();
            assertTrue(true);
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
    public void testUpdateNV_testChuan() throws Exception {
        ModelNhanVien nv = new ModelNhanVien(100, "Nguyen Hoang Viet", "10-05-2023", "0123456789789", "Quan ly", 100);

        try {
            con.setAutoCommit(false);
            sa.updateNV(nv);

            ModelNhanVien after = sa.getNV(100);

            assertEquals(nv.getId_NV(), after.getId_NV());
            assertEquals(nv.getTenNV(), after.getTenNV());
            assertEquals(nv.getNgayVL(), after.getNgayVL());
            assertEquals(nv.getChucvu(), after.getChucvu());
            assertEquals(nv.getId_NQL(), after.getId_NQL());

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
    public void testUpdateNV_DuLieuKhongDoi() throws Exception {
        ModelNhanVien nv = new ModelNhanVien(100, "Nguyen Hoang Viet2", "10-05-2023", "0123456789789", "Quan ly", 100);

        try {
            con.setAutoCommit(false);
            sa.updateNV(nv);

            ModelNhanVien after = sa.getNV(100);

            assertEquals(nv.getId_NV(), after.getId_NV());
            assertEquals(nv.getTenNV(), after.getTenNV());
            assertEquals(nv.getNgayVL(), after.getNgayVL());
            assertEquals(nv.getChucvu(), after.getChucvu());
            assertEquals(nv.getId_NQL(), after.getId_NQL());

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
    public void testUpdateNV_KhuyetMotSoTruong() throws Exception {
        ModelNhanVien nv = new ModelNhanVien(100, "Nguyen Hoang Viet", "10-05-2023", null, "Quan ly", 100);

        try {
            con.setAutoCommit(false);
            sa.updateNV(nv);

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
    public void testUpdateNV_TenNhanVienQuaDai() throws Exception {
        ModelNhanVien nv = new ModelNhanVien(100,
                "Nguyen Hoang Viet Nguyen Hoang Viet Nguyen Hoang Viet Nguyen Hoang Viet Nguyen Hoang Viet Nguyen Hoang Viet",
                "10-05-2023", "0123456789789", "Quan ly", 100);

        try {
            con.setAutoCommit(false);
            sa.updateNV(nv);
            fail("Expected SQLException was not thrown");
        } catch (SQLException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains("ORA"));
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
    public void testUpdateNV_TruongDuLieuKhongHopLe() throws Exception {
        ModelNhanVien nv = new ModelNhanVien(100,
                "Nguyen Hoang Viet",
                "10-05-2023", "0123456789789", "Lai xe", 100);

        try {
            con.setAutoCommit(false);
            sa.updateNV(nv);
            fail("Expected SQLException was not thrown");
        } catch (SQLException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains("ORA"));
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
    public void testUpdateNV_IDBangKhong() throws Exception {
        ModelNhanVien nv = new ModelNhanVien(0,
                "Nguyen Hoang Viet",
                "10-05-2023", "0123456789789", "Lai xe", 100);

        try {
            con.setAutoCommit(false);
            sa.updateNV(nv);
            fail("Expected SQLException was not thrown");
        } catch (SQLException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains("ORA"));
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
    public void testUpdateNV_IDKhongTonTai() throws Exception {
        ModelNhanVien nv = new ModelNhanVien(99999,
                "Nguyen Hoang Viet",
                "10-05-2023", "0123456789789", "Lai xe", 100);

        try {
            con.setAutoCommit(false);
            sa.updateNV(nv);
            fail("Expected SQLException was not thrown");
        } catch (SQLException e) {
            e.printStackTrace();
            assertTrue(e.getMessage().contains("ORA"));
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
    public void testGetRevenueHD_HomNay() throws Exception {

        try {
            con.setAutoCommit(false);
            assertEquals(250000, sa.getRevenueHD("Hôm nay"));
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
    public void testGetRevenueHD_ThangNay() throws Exception {

        try {
            con.setAutoCommit(false);
            assertEquals(250000, sa.getRevenueHD("Tháng này"));
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
    public void testGetRevenueHD_NamNay() throws Exception {

        try {
            con.setAutoCommit(false);
            assertEquals(250000, sa.getRevenueHD("Năm này"));
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
    public void testGetRevenueHD_KhongHopLe() throws Exception {

        try {
            con.setAutoCommit(false);
            assertNotNull(sa.getRevenueHD("Năm kia"));
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
    public void testGetPreMonthRevenueHD() throws Exception {
        try {
            con.setAutoCommit(false);
            assertNotNull(sa.getPreMonthRevenueHD());
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
    public void testGetCostNK() throws Exception {
    }

    @Test
    public void testGetPreMonthCostNK() throws Exception {
    }

    @Test
    public void testGetRevenueCostProfit_byMonth() throws Exception {
    }

    @Test
    public void testGetMenuFood() throws Exception {
        //thực đơn đang có 89 món ăn với id từ 1-89
        ArrayList<ModelMonAn> menu = sa.getMenuFood();

        assertNotNull(menu);
        assertEquals(89, menu.size());
    }

    @Test
    public void testGetNumberFood_inBusiness() throws Exception {
        //thực đơn đang có 89 món ăn đang kinh doanh
        ArrayList<ModelMonAn> menu = sa.getMenuFood();

        assertNotNull(menu);
        assertEquals(89, menu.size());
    }

    @Test
    public void testInsertMA_testChuan() throws Exception {

        //thực đơn đang có 89 món ăn với id từ 1-89
        ModelMonAn ma = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")),
                90, "Thịt chó nấu rựa mận", 350000, "Gemini", "Dang kinh doanh"
        );

        try {
            con.setAutoCommit(false);
            sa.insertMA(ma);

            ArrayList<ModelMonAn> ma2 = sa.getMenuFood();

            assertNotNull(ma2);
            assertEquals(90, ma2.size());

        } catch (SQLException e) {
            fail(e.getMessage());
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
    public void testInsertMA_monAnDaTonTai() throws Exception {

        //thực đơn đang có 89 món ăn với id từ 1-89
        ModelMonAn ma = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")),
                90, "DUI CUU NUONG XE NHO", 250000, "Aries", "Dang kinh doanh"
        );

        try {
            con.setAutoCommit(false);
            sa.insertMA(ma);

            ArrayList<ModelMonAn> ma2 = sa.getMenuFood();

            assertNotNull(ma2);
            assertEquals(89, ma2.size());

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
    public void testInsertMA_monAnRong() throws Exception {

        //thực đơn đang có 89 món ăn với id từ 1-89
        ModelMonAn ma = null;

        try {
            con.setAutoCommit(false);
            sa.insertMA(ma);

            fail("Món ăn rỗng mà vẫn thêm được");

        } catch (NullPointerException e) {
            e.printStackTrace();
            assertTrue(true);
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
    public void testInsertMA_monAnKhuyetMatTruongBatBuoc() throws Exception {

        //thực đơn đang có 89 món ăn với id từ 1-89
        ModelMonAn ma = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")),
                90, null, 250000, "Aries", "Dang kinh doanh"
        );

        try {
            con.setAutoCommit(false);
            sa.insertMA(ma);

            fail("Món ăn không có tên mà vẫn thêm được");

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(true);
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
    public void testInsertMA_dinhDangDuLieuSai() throws Exception {

        //thực đơn đang có 89 món ăn với id từ 1-89
        ModelMonAn ma = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")),
                90, "Thịt chó nấu rựa mận", 350000, "Sơn hào hải vị", "Dang kinh doanh"
        );

        try {
            con.setAutoCommit(false);
            sa.insertMA(ma);

            fail("Loại món ăn không tồn tại mà vẫn thêm được");

        } catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            assertTrue(true);
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
    public void testUpdateMonAn_testChuan() throws Exception {
        //Trước cập nhật
        ArrayList<ModelMonAn> arr1 = sa.getMenuFood();

        //giá gốc là 250000
        ModelMonAn ma = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")),
                1, "DUI CUU NUONG XE NHO", 257000, "Aries", "Dang kinh doanh"
        );

        try {
            con.setAutoCommit(false);
            sa.UpdateMonAn(ma);

            ArrayList<ModelMonAn> arr2 = sa.getMenuFood();

            assertEquals(arr1.size(), arr2.size());
            assertEquals(arr1.get(0).getId(), arr2.get(0).getId());
            assertNotEquals(arr1.get(0).getValue(), arr2.get(0).getValue());

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
    public void testUpdateMonAn_duLieuKhongDoi() throws Exception {
        //Trước cập nhật
        ArrayList<ModelMonAn> arr1 = sa.getMenuFood();

        //giá gốc là 250000
        ModelMonAn ma = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")),
                1, "DUI CUU NUONG XE NHO", 250000, "Aries", "Dang kinh doanh"
        );

        try {
            con.setAutoCommit(false);
            sa.UpdateMonAn(ma);

            ArrayList<ModelMonAn> arr2 = sa.getMenuFood();

            assertEquals(arr1.size(), arr2.size());
            assertEquals(arr1.get(0).getId(), arr2.get(0).getId());
            assertEquals(arr1.get(0).getValue(), arr2.get(0).getValue());

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
    public void testUpdateMonAn_duLieuKhongHopLe() throws Exception {

        ModelMonAn ma = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")),
                1, "DUI CUU NUONG XE NHO", -250000, "Aries", "Dang kinh doanh"
        );

        try {
            con.setAutoCommit(false);
            sa.UpdateMonAn(ma);

            fail("Đơn giá âm");

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(true);
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
    public void testUpdateMonAn_IdBangKhong() throws Exception {

        ModelMonAn ma = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")),
                0, "DUI CUU NUONG XE NHO", 250000, "Aries", "Dang kinh doanh"
        );

        try {
            con.setAutoCommit(false);
            sa.UpdateMonAn(ma);

            fail("ID = 0");

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(true);
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
    public void testUpdateMonAn_IdBangAm() throws Exception {

        ModelMonAn ma = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")),
                -10, "DUI CUU NUONG XE NHO", 250000, "Aries", "Dang kinh doanh"
        );

        try {
            con.setAutoCommit(false);
            sa.UpdateMonAn(ma);

            fail("ID âm");

        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(true);
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
    public void testUpdateMonAn_TenBiTrung() throws Exception {

        ArrayList<ModelMonAn> arr = sa.getMenuFood();

        ModelMonAn ma = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")),
                2, "DUI CUU NUONG XE NHO", 250000, "Aries", "Dang kinh doanh"
        );

        try {
            con.setAutoCommit(false);
            sa.UpdateMonAn(ma);

            ArrayList<ModelMonAn> arr2 = sa.getMenuFood();

            assertNotEquals(arr.get(1).getTitle(), arr2.get(1).getTitle());
            assertNotEquals(arr2.get(0).getTitle(), arr2.get(0).getTitle());

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
    public void testUpdateMonAn_khuyetMotSoTruong() throws Exception {

        ArrayList<ModelMonAn> arr = sa.getMenuFood();

        ModelMonAn ma = new ModelMonAn(new ImageIcon(getClass().getResource("/Icons/Food/Unknown/unknown.jpg")),
                1, null, 250000, "Aries", "Dang kinh doanh"
        );

        try {
            con.setAutoCommit(false);
            sa.UpdateMonAn(ma);

            ArrayList<ModelMonAn> arr2 = sa.getMenuFood();

            assertEquals(arr.size(), arr2.size());
            assertNotNull(arr2.get(0).getTitle());

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
    // hàm test với trường hợp "tất cả" hoá đơn
    public void testGetListHDIn_All() throws SQLException {
        ArrayList<ModelHoaDon> list = sa.getListHDIn("Tất cả");

        assertEquals(3, list.size()); // cơ sở dữ liệu 2 bản ghi mong dợi 2
        assertEquals(0, list.get(1).getIdHoaDon()); // Kiểm tra ID_HoaDon của hóa đơn đầu tiên
    }

    

}
