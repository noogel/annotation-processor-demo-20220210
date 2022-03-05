package noogel.xyz.getter;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("noogel.xyz.getter.Getter")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class GetterProcessor extends AbstractProcessor {
    // 打印log
    private Messager messager;
    // 抽象语法树
    private JavacTrees trees;
    private TreeMaker treeMaker;
    private Names names;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        trees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        treeMaker = TreeMaker.instance(context);
        names = Names.instance(context);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            Set<? extends Element> annotatedWith = roundEnv.getElementsAnnotatedWith(Getter.class);
            for (Element element : annotatedWith) {
                JCTree jcTree = trees.getTree(element);
                jcTree.accept(new TreeTranslator() {
                    @Override
                    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                        List<JCTree.JCVariableDecl> jcVariableDeclList = List.nil();
                        // 获取所有属性
                        for (JCTree tree : jcClassDecl.defs) {
                            if (tree.getKind().equals(Tree.Kind.VARIABLE)) {
                                JCTree.JCVariableDecl jcVariableDecl = (JCTree.JCVariableDecl) tree;
                                jcVariableDeclList = jcVariableDeclList.append(jcVariableDecl);
                            }
                        }
                        // 为每一个属性创建get方法
                        jcVariableDeclList.forEach(jcVariableDecl -> {
                            messager.printMessage(Diagnostic.Kind.NOTE, jcVariableDecl.getName() + " has been processed");
                            jcClassDecl.defs = jcClassDecl.defs.prepend(makeGetterMethodDecl(jcVariableDecl));
                        });
                        super.visitClassDef(jcClassDecl);
                    }
                });
            }
        }
        return true;
    }

    private JCTree.JCMethodDecl makeGetterMethodDecl(JCTree.JCVariableDecl jcVariableDecl) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        statements.append(treeMaker.Return(treeMaker.Select(treeMaker.Ident(names.fromString("this")), jcVariableDecl.getName())));
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        return treeMaker.MethodDef(treeMaker.Modifiers(Flags.PUBLIC), getNewMethodName(jcVariableDecl.getName()), jcVariableDecl.vartype,
                List.nil(), List.nil(), List.nil(), body, null);
    }

    private Name getNewMethodName(Name name) {
        String s = name.toString();
        return names.fromString("get" + s.substring(0, 1).toUpperCase() + s.substring(1, name.length()));
    }
}
