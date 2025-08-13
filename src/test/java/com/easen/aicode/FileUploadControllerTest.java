package com.easen.aicode;

import com.easen.aicode.controller.FileUploadController;
import com.easen.aicode.manager.CosManager;
import com.easen.aicode.model.entity.User;
import com.easen.aicode.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 文件上传控制器测试
 */
@ExtendWith(MockitoExtension.class)
class FileUploadControllerTest {

    @Mock
    private CosManager cosManager;

    @Mock
    private UserService userService;

    @InjectMocks
    private FileUploadController fileUploadController;

    private MockHttpServletRequest request;
    private User mockUser;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUserName("testUser");
    }

    @Test
    void testUploadImage_Success() throws Exception {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(userService.getLoginUser(any())).thenReturn(mockUser);
        when(cosManager.uploadFile(anyString(), any())).thenReturn("https://example.com/images/test.jpg");

        // 执行测试
        var result = fileUploadController.uploadImage(file, request);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertEquals("https://example.com/images/test.jpg", result.getData());
    }

    @Test
    void testUploadImage_EmptyFile() {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                new byte[0]
        );

        // 执行测试并验证异常
        assertThrows(Exception.class, () -> {
            fileUploadController.uploadImage(file, request);
        });
    }

    @Test
    void testUploadImage_InvalidFileType() {
        // 准备测试数据
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        // 执行测试并验证异常
        assertThrows(Exception.class, () -> {
            fileUploadController.uploadImage(file, request);
        });
    }

    @Test
    void testUploadImages_Success() throws Exception {
        // 准备测试数据
        MockMultipartFile file1 = new MockMultipartFile(
                "files",
                "test1.jpg",
                "image/jpeg",
                "test image content 1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files",
                "test2.png",
                "image/png",
                "test image content 2".getBytes()
        );

        MockMultipartFile[] files = {file1, file2};

        when(userService.getLoginUser(any())).thenReturn(mockUser);
        when(cosManager.uploadFile(anyString(), any()))
                .thenReturn("https://example.com/images/test1.jpg")
                .thenReturn("https://example.com/images/test2.png");

        // 执行测试
        var result = fileUploadController.uploadImages(files, request);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertNotNull(result.getData());
        assertEquals(2, result.getData().length);
        assertEquals("https://example.com/images/test1.jpg", result.getData()[0]);
        assertEquals("https://example.com/images/test2.png", result.getData()[1]);
    }

    @Test
    void testDeleteImage_Success() {
        // 准备测试数据
        String imageUrl = "https://example.com/images/test.jpg";

        when(cosManager.extractKeyFromUrl(imageUrl)).thenReturn("/images/test.jpg");
        when(cosManager.deleteFile("/images/test.jpg")).thenReturn(true);

        // 执行测试
        var result = fileUploadController.deleteImage(imageUrl);

        // 验证结果
        assertNotNull(result);
        assertEquals(0, result.getCode());
        assertTrue(result.getData());
    }
}

