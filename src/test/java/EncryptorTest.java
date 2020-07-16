import com.Application;
import org.jasypt.encryption.StringEncryptor;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author wangdejun
 * @description: TODO description
 * @date 2020/7/16 16:05
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class EncryptorTest {

    @Autowired
    private StringEncryptor encryptor;


}
