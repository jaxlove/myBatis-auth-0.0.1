import org.apache.ibatis.javassist.ClassPool;
import org.apache.ibatis.javassist.CtClass;
import org.apache.ibatis.javassist.CtMethod;

/**
 * @author wangdejun
 * @description: test
 * @date 2020/7/16 16:26
 */
public class JavasisstTest {

    public void overrideMethod() {
        ClassPool classPool = ClassPool.getDefault();
        try {
            CtClass ctClass = classPool.get("Demo");
            CtMethod method = ctClass.getDeclaredMethod("test2");
            method.setBody("new com.Demo2().test2($1);");
//            ctClass.writeFile();
            //wdjtodo 该使用哪个类加载器，待确定
            ctClass.toClass(this.getClass().getClassLoader());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Demo demo = new Demo();
        demo.test2("哈哈");
    }


}
