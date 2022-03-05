package noogel.xyz.provider;

import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
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
import java.util.Objects;
import java.util.Set;

// process() 不生效意味着这里的路径配错了
@SupportedAnnotationTypes("noogel.xyz.provider.StrengthenBuilder")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class StringProviderProcessor extends AbstractProcessor {
    private Messager messager;
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
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv)  {
        if (!roundEnv.processingOver()) {
            Set<? extends Element> annotatedWith = roundEnv.getElementsAnnotatedWith(StrengthenBuilder.class);
            for (Element element : annotatedWith) {
                JCTree jcTree = trees.getTree(element);
                jcTree.accept(new TreeTranslator() {
                    @Override
                    public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                        List<JCTree.JCMethodDecl> jcMethodDecls = List.nil();
                        for (JCTree tree : jcClassDecl.defs) {
                            if (tree.getKind().equals(Tree.Kind.METHOD)) {
                                JCTree.JCMethodDecl jcMethodDecl = (JCTree.JCMethodDecl) tree;
                                boolean has = jcMethodDecl.mods.annotations.stream()
                                        .anyMatch(t -> Objects.equals(Strengthen.class.getName(), t.type.toString()));
                                if (has) {
                                    jcMethodDecls = jcMethodDecls.append(jcMethodDecl);
                                }
                            }
                        }
                        jcMethodDecls.forEach(jcMethodDecl -> {
//                            messager.printMessage(Diagnostic.Kind.NOTE, jcMethodDecl.getName() + " has been processed");
                            if (!hasVariableDecl(jcClassDecl, jcMethodDecl)) {
                                JCTree.JCVariableDecl jcVariableDecl = makeVariableDecl(jcMethodDecl);
                                jcClassDecl.defs = jcClassDecl.defs.prepend(jcVariableDecl);
                            }
                            jcClassDecl.defs = jcClassDecl.defs.prepend(makeMethodDecl(jcMethodDecl));
                        });
                        super.visitClassDef(jcClassDecl);
                    }
                });
            }
            return true;
        }
        return false;
    }

    private JCTree.JCVariableDecl makeVariableDecl(JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCAnnotation jcAnnotation = jcMethodDecl.mods.annotations.get(0);
        JCTree.JCAssign jcAssign = (JCTree.JCAssign) jcAnnotation.args.get(0);
        JCTree.JCFieldAccess jcFieldAccess = (JCTree.JCFieldAccess) jcAssign.rhs;
        String className = jcFieldAccess.selected.toString();
        Name name = names.fromString(className);
        JCTree.JCExpression varType = treeMaker.Ident(name);
        JCTree.JCNewClass init = treeMaker.NewClass(null, List.nil(), varType, List.nil(), null);
        return treeMaker.VarDef(treeMaker.Modifiers(Flags.PRIVATE | Flags.STATIC | Flags.FINAL),
                getLowerCamelCase(className), varType, init);
    }

    private boolean hasVariableDecl(JCTree.JCClassDecl jcClassDecl, JCTree.JCMethodDecl jcMethodDecl) {
        JCTree.JCAnnotation jcAnnotation = jcMethodDecl.mods.annotations.get(0);
        JCTree.JCAssign jcAssign = (JCTree.JCAssign) jcAnnotation.args.get(0);
        JCTree.JCFieldAccess jcFieldAccess = (JCTree.JCFieldAccess) jcAssign.rhs;
        String className = jcFieldAccess.selected.toString();
        Name name = getLowerCamelCase(className);
        return jcClassDecl.defs.stream().filter(t-> t.getKind().equals(Tree.Kind.VARIABLE))
                .map(t-> (JCTree.JCVariableDecl) t).anyMatch(t-> t.name.equals(name));
    }

    private JCTree.JCMethodDecl makeMethodDecl(JCTree.JCMethodDecl jcMethodDecl) {
        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        JCTree.JCAnnotation jcAnnotation = jcMethodDecl.mods.annotations.get(0);
        JCTree.JCAssign jcAssignVal = (JCTree.JCAssign) jcAnnotation.args.get(0);
        JCTree.JCAssign jcAssignParam = (JCTree.JCAssign) jcAnnotation.args.get(1);
        JCTree.JCFieldAccess jcFieldAccessParam = (JCTree.JCFieldAccess) jcAssignParam.rhs;
        JCTree.JCFieldAccess jcFieldAccessVal = (JCTree.JCFieldAccess) jcAssignVal.rhs;
        String classNameParam = jcFieldAccessParam.selected.toString();
        String classNameVal = jcFieldAccessVal.selected.toString();
        Name nameParam = names.fromString(classNameParam);
        Name nameVal = names.fromString(classNameVal);
        JCTree.JCVariableDecl param = treeMaker.VarDef(
                treeMaker.Modifiers(Flags.PARAMETER, List.nil()),
                getLowerCamelCase(classNameParam),
                treeMaker.Ident(nameParam),
                null);

        statements.append(treeMaker.Return(
                treeMaker.Apply(
                        List.nil(),
                        treeMaker.Select(treeMaker.Ident(getLowerCamelCase(classNameVal)), names.fromString("getResp")),
                        List.of(treeMaker.Ident(getLowerCamelCase(classNameParam)),
                                treeMaker.Reference(MemberReferenceTree.ReferenceMode.INVOKE,
                                        jcMethodDecl.getName(),
                                        treeMaker.Ident(names.fromString("this")), null)
                        )
                )
        ));
        JCTree.JCBlock body = treeMaker.Block(0, statements.toList());
        JCTree.JCExpression returnType = (JCTree.JCExpression) jcMethodDecl.getReturnType();
        JCTree.JCMethodDecl jcMethodDeclNew = treeMaker.MethodDef(
                treeMaker.Modifiers(Flags.PUBLIC),
                jcMethodDecl.getName(),
                returnType,
                List.nil(),
                List.of(param),
                List.nil(),
                body,
                null);
        return jcMethodDeclNew;
    }

    public Name getLowerCamelCase(String name) {
        String lowerName = name.substring(0, 1).toLowerCase() + name.substring(1) + "AutoGen";
        return names.fromString(lowerName);
    }
}
