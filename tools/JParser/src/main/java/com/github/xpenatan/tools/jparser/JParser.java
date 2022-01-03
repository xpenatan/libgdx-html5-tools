package com.github.xpenatan.tools.jparser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.utils.PositionUtils;
import com.github.xpenatan.tools.jparser.codeparser.CodeParser;
import com.github.xpenatan.tools.jparser.codeparser.CodeParserItem;
import com.github.xpenatan.tools.jparser.util.CustomFileDescriptor;
import com.github.xpenatan.tools.jparser.util.CustomPrettyPrinter;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Optional;

/**
 *  JParser is a simple solution to change the original java source with a custom code.
 *  It searches for code blocks with a specific tag to change it.
 *
 *  How it works:
 *  The DefaultCodeParser searches for a header tag staring with '[-HEADER' and ending with ']'.
 *  Then it will call the code parser listener, so it can modify the original source code.
 *
 *  DefaultCodeParser is an abstract class that does most of the work to -ADD, -REPLACE or -REMOVE the code block with your own custom code.
 *  The -NATIVE tag needs to be implemented, and it only works with native methods.
 *  DefaultCodeParser will remove the code block automatically if the HEADER tag does not match.
 *
 *  @author xpenatan */
public class JParser {

    static String gen = "-------------------------------------------------------\n"
            + " * This file was generated by JParser\n"
            + " *\n * Do not make changes to this file\n"
            + " *-------------------------------------------------------";

    CustomFileDescriptor sourceDir;
    CustomFileDescriptor genDir;
    String[] excludes;
    CodeParser wrapper;

    public void generate(String sourceDir, String genDir, CodeParser wrapper, String[] excludes) throws Exception {
        this.excludes = excludes;
        this.wrapper = wrapper;
        this.sourceDir = new CustomFileDescriptor(sourceDir);
        this.genDir = new CustomFileDescriptor(genDir);

        // check if source directory exists
        if(!this.sourceDir.exists()) {
            throw new Exception("Java source directory '" + sourceDir + "' does not exist");
        }

        if(!this.genDir.exists()) {
            if(!this.genDir.mkdirs()) {
                throw new Exception("Couldn't create directory '" + genDir + "'");
            }
        }
        else {
            this.genDir.deleteDirectory();
            if(!this.genDir.mkdirs()) {
                throw new Exception("Couldn't create directory '" + genDir + "'");
            }
        }
        System.out.println("***** GENERATING CODE *****");
        processDirectory(this.sourceDir);
        System.out.println("********** DONE ***********");
    }

    private void processDirectory(CustomFileDescriptor dir) throws Exception {
        CustomFileDescriptor[] files = dir.list();
        for(CustomFileDescriptor file : files) {
            if(file.isDirectory()) {
                if(file.path().contains(".svn")) continue;
                if(file.path().contains(".git")) continue;
                processDirectory(file);
            }
            else {
                if(file.extension().equals("java")) {
                    boolean stop = false;
                    if(excludes != null) {
                        for(int i = 0; i < excludes.length; i++) {
                            String path = file.path();
                            String exclude = excludes[i];

                            if(exclude.startsWith("!")) {
                                String substring = exclude.substring(1);
                                if(path.contains(substring)) {
                                    stop = false;
                                    break;
                                }
                            }
                            else if(path.contains(exclude)) {
                                stop = true;
                            }
                        }
                    }

                    if(stop)
                        continue;

                    String className = getFullyQualifiedClassName(file);
                    CustomFileDescriptor codeFile = new CustomFileDescriptor(genDir + "/" + className + ".cpp");
                    if(file.lastModified() < codeFile.lastModified()) {
                        System.out.println("Code for '" + file.path() + "' up to date");
                        continue;
                    }
                    String javaContent = file.readString();
                    System.out.println("Parsing: " + file);
                    String codeParsed = parseClass(javaContent);
                    generateFile(file, codeParsed);
                }
            }
        }
    }

    private void generateFile(CustomFileDescriptor fileName, String javaContent) {
        String packageFilePath = fileName.file().getAbsolutePath().replace(sourceDir.file().getAbsolutePath(), "");
        String fullPath = genDir.file().getAbsolutePath() + packageFilePath;
        CustomFileDescriptor fileDescriptor = new CustomFileDescriptor(fullPath);
        fileDescriptor.writeString(javaContent, false);
    }

    private String getFullyQualifiedClassName(CustomFileDescriptor file) {
        String className = file.path().replace(sourceDir.path(), "").replace('\\', '.').replace('/', '.').replace(".java", "");
        if(className.startsWith(".")) className = className.substring(1);
        return className;
    }

    private String parseClass(String javaContent) throws Exception {
        CompilationUnit unit = StaticJavaParser.parse(new ByteArrayInputStream(javaContent.getBytes()));
        unit.printer(new CustomPrettyPrinter());

        ArrayList<BlockComment> blockComments = new ArrayList<>();

        ArrayList<Node> array = new ArrayList<>();
        array.addAll(unit.getChildNodes());
        PositionUtils.sortByBeginPosition(array, false);
        for(int i = 0; i < array.size(); i++) {
            Node node = array.get(i);

            if(node instanceof BlockComment) {
                BlockComment blockComment = (BlockComment) node;
                blockComments.add(blockComment);
            }
            else if(node instanceof ImportDeclaration) {
                ImportDeclaration importDeclaration = (ImportDeclaration) node;
                Optional<Comment> optionalComment = importDeclaration.getComment();
                boolean addImport = false;
                if(optionalComment.isPresent()) {
                    Comment comment = optionalComment.get();
                    if(comment instanceof BlockComment) {
                        BlockComment blockComment = (BlockComment) optionalComment.get();
                        blockComments.add(blockComment);
                        addImport = true;
                    }
                    addBlockCommentItem(unit, true, wrapper, null, blockComments, null, null, addImport ? importDeclaration : null);
                }
            }
            else {
                addBlockCommentItem(unit, true, wrapper, null, blockComments, null, null, null);

                if(node instanceof PackageDeclaration) {
                    PackageDeclaration packageD = (PackageDeclaration) node;
                    packageD.setComment(new BlockComment(gen));
                }
                else if(node instanceof ClassOrInterfaceDeclaration) {
                    parseClassInterface(unit, wrapper, (ClassOrInterfaceDeclaration) node, 0);
                }
            }
        }
        PositionUtils.sortByBeginPosition(unit.getTypes(), false);

        return unit.toString();
    }

    private static void parseClassInterface(CompilationUnit unit, CodeParser wrapper, ClassOrInterfaceDeclaration clazzInterface, int classLevel) {
        ArrayList<Node> array = new ArrayList<>();
        array.addAll(clazzInterface.getChildNodes());
        PositionUtils.sortByBeginPosition(array, false);
        ArrayList<BlockComment> blockComments = new ArrayList<>();

        for(int i = 0; i < array.size(); i++) {
            Node node = array.get(i);

            if(node instanceof BlockComment) {
                BlockComment blockComment = (BlockComment) node;
                blockComments.add(blockComment);
            }
            else if(node instanceof FieldDeclaration) {
                FieldDeclaration field = (FieldDeclaration) node;
                Optional<Comment> optionalComment = field.getComment();

                boolean addField = false;

                if(optionalComment.isPresent()) {
                    Comment comment = optionalComment.get();
                    if(comment instanceof BlockComment) {
                        BlockComment blockComment = (BlockComment) optionalComment.get();
                        blockComments.add(blockComment);
                        addField = true;
                    }
                }
                addBlockCommentItem(unit, false, wrapper, clazzInterface, blockComments, addField ? field : null, null, null);
            }
            else if(node instanceof MethodDeclaration) {
                MethodDeclaration method = (MethodDeclaration) node;
                Optional<Comment> optionalComment = method.getComment();

                boolean addMethod = false;
                if(optionalComment.isPresent()) {
                    if(optionalComment.get() instanceof BlockComment) {
                        BlockComment blockComment = (BlockComment) optionalComment.get();
                        blockComments.add(blockComment);
                        addMethod = true;
                    }
                }
                addBlockCommentItem(unit, false, wrapper, clazzInterface, blockComments, null, addMethod ? method : null, null);
            }
            else {
                addBlockCommentItem(unit, false, wrapper, clazzInterface, blockComments, null, null, null);
                if(node instanceof ClassOrInterfaceDeclaration) {
                    parseClassInterface(unit, wrapper, (ClassOrInterfaceDeclaration) node, ++classLevel);
                }
            }
        }

        if(classLevel == 0) {
            addBlockCommentItem(unit, false, wrapper, clazzInterface, blockComments, null, null, null);
        }

        PositionUtils.sortByBeginPosition(clazzInterface.getMembers(), false);
    }

    private static boolean addBlockCommentItem(CompilationUnit unit, boolean isHeader, CodeParser wrapper, ClassOrInterfaceDeclaration classInterface, ArrayList<BlockComment> blockComments, FieldDeclaration field, MethodDeclaration method, ImportDeclaration importDeclaration) {
        if(blockComments.size() > 0) {
            CodeParserItem parserItem = new CodeParserItem();
            parserItem.unit = unit;
            parserItem.rawComments.addAll(blockComments);
            parserItem.classInterface = classInterface;
            parserItem.fieldDeclaration = field;
            parserItem.methodDeclaration = method;
            parserItem.importDeclaration = importDeclaration;
            blockComments.clear();
            if(isHeader) {
                wrapper.parseHeaderBlock(parserItem);
            }
            else {
                wrapper.parseCodeBlock(parserItem);
            }
            return true;
        }
        return false;
    }
}
