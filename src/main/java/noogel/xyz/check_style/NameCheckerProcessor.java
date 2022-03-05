package noogel.xyz.check_style;

import noogel.xyz.provider.StrengthenBuilder;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

@SupportedAnnotationTypes("noogel.xyz.check_style.NameChecker")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class NameCheckerProcessor extends AbstractProcessor {

    private NameCheckHelper helper;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        helper = new NameCheckHelper(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            Set<? extends Element> annotatedWith = roundEnv.getElementsAnnotatedWith(NameChecker.class);
            for (Element element : annotatedWith) {
                helper.checkNames(element);
            }
        }
        return false;
    }
}