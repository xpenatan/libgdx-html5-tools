package com.github.xpenatan.gdx.backends.teavm.config.plugins;

import com.github.xpenatan.gdx.backends.teavm.dom.CanvasRenderingContext2DWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.DocumentWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.ElementWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.EventHandlerWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.EventListenerWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.EventWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.HTMLCanvasElementWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.HTMLDocumentWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.HTMLElementWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.HTMLVideoElementWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.KeyboardEventWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.LocationWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.MouseEventWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.NodeWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.ProgressEventWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.StyleWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.TouchEventWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.TouchListWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.WebJSObject;
import com.github.xpenatan.gdx.backends.teavm.dom.WheelEventWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.XMLHttpRequestEventTargetWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.XMLHttpRequestWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.typedarray.FloatArrayWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.typedarray.LongArrayWrapper;
import com.github.xpenatan.gdx.backends.teavm.dom.typedarray.ObjectArrayWrapper;
import com.github.xpenatan.gdx.backends.teavm.gen.Emulate;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;
import org.teavm.model.AnnotationContainer;
import org.teavm.model.AnnotationHolder;
import org.teavm.model.AnnotationValue;
import org.teavm.model.ClassHierarchy;
import org.teavm.model.ClassHolder;
import org.teavm.model.ClassHolderTransformer;
import org.teavm.model.ClassHolderTransformerContext;
import org.teavm.model.ClassReader;
import org.teavm.model.ClassReaderSource;
import org.teavm.model.ElementModifier;
import org.teavm.model.FieldHolder;
import org.teavm.model.FieldReader;
import org.teavm.model.MethodDescriptor;
import org.teavm.model.MethodHolder;
import org.teavm.model.MethodReader;
import org.teavm.model.ReferenceCache;
import org.teavm.model.emit.ProgramEmitter;
import org.teavm.model.util.ModelUtils;
import org.teavm.parsing.ClassRefsRenamer;

/**
 * @author xpenatan
 */
public class TeaClassTransformer implements ClassHolderTransformer {

    private boolean init = false;

    public static String applicationListener = "";
    public static String mainClass = "";
    public static Reflections reflections = new Reflections("emulate", "emu", "com", "org", "emulate/net");

    private HashMap<String, Class<?>> emulations = new HashMap<>();
    private HashMap<String, String> emulations2 = new HashMap<>();
    private HashMap<String, Class<?>> updateCode = new HashMap<>();
    private ReferenceCache referenceCache = new ReferenceCache();

    public TeaClassTransformer() {
        Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Emulate.class);
        for(Class<?> type : annotated) {
            Emulate em = type.getAnnotation(Emulate.class);
            if(em == null) {
                continue;
            }
            Class<?> toEmulate = em.value();
            String typeName = null;
            if(toEmulate == Object.class) {
                typeName = em.valueStr();
            }
            else {
                typeName = toEmulate.getTypeName();
            }
            typeName = typeName.trim();
            if(!typeName.isEmpty()) {
                emulations2.put(type.getName(), typeName);
                if(em.updateCode()) {
                    updateCode.put(typeName, type);
                }
                else {
                    emulations.put(typeName, type);
                }
            }
        }
    }

    @Override
    public void transformClass(ClassHolder cls, ClassHolderTransformerContext context) {
        ClassReaderSource innerSource = context.getHierarchy().getClassSource();
        if(!init) {
            init = true;
            ClassHolder classHolder = null;

            if(!applicationListener.isEmpty()) {
                classHolder = findClassHolder(cls, context, TeaClassTransformer.mainClass);
                setGdxApplicationClass(classHolder, context, TeaClassTransformer.applicationListener);
            }

            classHolder = findClassHolder(cls, context, ObjectArrayWrapper.class);
            setClassInterface(classHolder, JSObject.class);

            classHolder = findClassHolder(cls, context, LongArrayWrapper.class);
            setClassInterface(classHolder, JSObject.class);

            classHolder = findClassHolder(cls, context, HTMLVideoElementWrapper.class);
            setClassInterface(classHolder, JSObject.class);

            classHolder = findClassHolder(cls, context, HTMLCanvasElementWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getWidth", null);
            setMethodAnnotation(classHolder, JSProperty.class, "setWidth", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getHeight", null);
            setMethodAnnotation(classHolder, JSProperty.class, "setHeight", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getOwnerDocument", null);

            classHolder = findClassHolder(cls, context, FloatArrayWrapper.class);
            setClassInterface(classHolder, JSObject.class);

            classHolder = findClassHolder(cls, context, EventListenerWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setClassAnnotation(classHolder, JSFunctor.class);

            Event
            classHolder = findClassHolder(cls, context, EventWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getTarget", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getType", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getDetail", null);

            classHolder = findClassHolder(cls, context, WheelEventWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getDeltaX", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getDeltaY", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getDeltaZ", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getWheelDelta", null);

            classHolder = findClassHolder(cls, context, MouseEventWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getClientX", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getClientY", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getMovementX", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getMovementY", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getButton", null);

            classHolder = findClassHolder(cls, context, KeyboardEventWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getCharCode", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getKeyCode", null);

            classHolder = findClassHolder(cls, context, TouchEventWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getChangedTouches", null);

            classHolder = findClassHolder(cls, context, TouchListWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getLength", null);

            classHolder = findClassHolder(cls, context, DocumentWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getCompatMode", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getDocumentElement", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getBody", null);

            classHolder = findClassHolder(cls, context, ElementWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getScrollTop", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getScrollLeft", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getClientWidth", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getClientHeight", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getStyle", null);

            classHolder = findClassHolder(cls, context, HTMLElementWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getOffsetParent", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getOffsetTop", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getOffsetLeft", null);

            classHolder = findClassHolder(cls, context, NodeWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getParentNode", null);

            classHolder = findClassHolder(cls, context, HTMLDocumentWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getParentNode", null);

            classHolder = findClassHolder(cls, context, EventHandlerWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setClassAnnotation(classHolder, JSFunctor.class);

            classHolder = findClassHolder(cls, context, XMLHttpRequestEventTargetWrapper.class);
            setMethodAnnotation(classHolder, JSProperty.class, "setOnprogress", null);
            setClassInterface(classHolder, JSObject.class);

            classHolder = findClassHolder(cls, context, XMLHttpRequestWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "setOnreadystatechange", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getReadyState", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getStatus", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getResponse", null);
            setMethodAnnotation(classHolder, JSProperty.class, "setResponseType", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getResponseText", null);

            classHolder = findClassHolder(cls, context, ProgressEventWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getLoaded", null);

            classHolder = findClassHolder(cls, context, LocationWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getHref", null);

            classHolder = findClassHolder(cls, context, WebJSObject.class);
            setClassInterface(classHolder, JSObject.class);

            classHolder = findClassHolder(cls, context, CanvasRenderingContext2DWrapper.class);
            setClassInterface(classHolder, JSObject.class);
            setMethodAnnotation(classHolder, JSProperty.class, "getGlobalAlpha", null);
            setMethodAnnotation(classHolder, JSProperty.class, "setGlobalAlpha", null);
            setMethodAnnotation(classHolder, JSProperty.class, "setFillStyle", null);
            setMethodAnnotation(classHolder, JSProperty.class, "setStrokeStyle", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getStrokeStyle", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getGlobalCompositeOperation", null);
            setMethodAnnotation(classHolder, JSProperty.class, "setGlobalCompositeOperation", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getLineWidth", null);
            setMethodAnnotation(classHolder, JSProperty.class, "setLineWidth", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getLineCap", null);
            setMethodAnnotation(classHolder, JSProperty.class, "setLineCap", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getLineJoin", null);
            setMethodAnnotation(classHolder, JSProperty.class, "setLineJoin", null);
            setMethodAnnotation(classHolder, JSProperty.class, "getMiterLimit", null);
            setMethodAnnotation(classHolder, JSProperty.class, "setMiterLimit", null);

            classHolder = findClassHolder(cls, context, StyleWrapper.class);
            setClassInterface(classHolder, JSObject.class);
        }

        emulateClass(context, cls, innerSource);
    }

    private void emulateClass(ClassHolderTransformerContext context, ClassHolder cls, ClassReaderSource innerSource) {
        String className = cls.getName();
        if(updateCode.containsKey(className)) {
            ClassReader original = innerSource.get(className);
            Class<?> emulated = updateCode.get(className);
            ClassReader emulatedClassHolder = innerSource.get(emulated.getName());
            if(emulatedClassHolder != null) {
                replaceClassCode(innerSource, emulated, cls, emulatedClassHolder);

                // teavm makes bunch of classreader copies. We also need to update the original class
                replaceClassCode(innerSource, emulated, (ClassHolder)original, emulatedClassHolder);
            }
        }
        else if(emulations.containsKey(className)) {
            ClassReader original = innerSource.get(className);
            Class<?> emulated = emulations.get(className);
            ClassReader emulatedClassHolder = innerSource.get(emulated.getName());
            if(emulatedClassHolder != null) {
                replaceClass(innerSource, cls, emulatedClassHolder);

                // teavm makes bunch of classreader copies. We also need to update the original class
                replaceClass(innerSource, (ClassHolder)original, emulatedClassHolder);
            }
        }
    }

    private void setGdxApplicationClass(ClassHolder classHolder, ClassHolderTransformerContext context, String entryPoint) {
        MethodHolder method = null;
        for(MethodHolder methodHolder : classHolder.getMethods()) {
            MethodDescriptor descriptor = methodHolder.getDescriptor();
            String string = descriptor.toString();
            if(string.contains("getApplicationListener()")) {
                method = methodHolder;
                break;
            }
        }
        if(method != null) {
            EnumSet<ElementModifier> modifiers = method.getModifiers();
            modifiers.remove(ElementModifier.NATIVE);
            ClassHierarchy hierarchy = context.getHierarchy();
            ProgramEmitter pe = ProgramEmitter.create(method, hierarchy);
            pe.construct(entryPoint).cast(Object.class).returnValue();
        }
    }

    private MethodHolder getMethodHolder(ClassHolder classHolder, String matchText) {
        Collection<MethodHolder> methods = classHolder.getMethods();
        Iterator<MethodHolder> iterator = methods.iterator();
        while(iterator.hasNext()) {
            MethodHolder methodHolder = iterator.next();
            MethodDescriptor descriptor = methodHolder.getDescriptor();
            String string = descriptor.toString();
            if(string.contains(matchText)) {
                return methodHolder;
            }
        }
        return null;
    }

    private ClassHolder findClassHolder(ClassHolder cur, ClassHolderTransformerContext context, Class clazz) {
        return findClassHolder(cur, context, clazz.getName());
    }

    private ClassHolder findClassHolder(ClassHolder cur, ClassHolderTransformerContext context, String clazz) {
        if(cur.getName().equals(clazz))
            return cur;
        ClassReaderSource innerSource = context.getHierarchy().getClassSource();
        ClassReader classReader = innerSource.get(clazz);
        ClassHolder classHolder = (ClassHolder)classReader;
        return classHolder;
    }

    private void setFieldAnnotation(ClassHolder classHolder, Class annotationClass, String fieldStr, String value) {
        for(FieldHolder field : classHolder.getFields().toArray(new FieldHolder[0])) {
            String name = field.getName();
            if(name.equals(fieldStr)) {
                AnnotationContainer annotations = field.getAnnotations();
                AnnotationHolder annotation = new AnnotationHolder(annotationClass.getName());
                if(value != null) {
                    annotation.getValues().put("value", new AnnotationValue(value));
                }
                annotations.add(annotation);
            }
        }
    }

    private void setMethodAnnotation(ClassHolder classHolder, Class annotationClass, String methodStr, String value) {
        for(MethodHolder method : classHolder.getMethods().toArray(new MethodHolder[0])) {
            String name = method.getName();
            if(name.equals(methodStr)) {
                AnnotationContainer annotations = method.getAnnotations();
                AnnotationHolder annotation = new AnnotationHolder(annotationClass.getName());
                if(value != null) {
                    annotation.getValues().put("value", new AnnotationValue(value));
                }
                annotations.add(annotation);
            }
        }
    }

    private void setClassInterface(ClassHolder classHolder, Class annotationClass) {
        Set<String> interfaces = classHolder.getInterfaces();
        interfaces.add(annotationClass.getName());
    }

    private void setClassAnnotation(ClassHolder classHolder, Class annotationClass) {
        AnnotationContainer annotations = classHolder.getAnnotations();
        AnnotationHolder annotation = new AnnotationHolder(annotationClass.getName());
        annotations.add(annotation);
    }

    private void replaceClassCode(ClassReaderSource innerSource, Class<?> emulated, final ClassHolder cls, final ClassReader emuCls) {
        Predicate<AnnotatedElement> annotatedElementPredicate = ReflectionUtils.withAnnotation(Emulate.class);
        Set<Field> fields = ReflectionUtils.getFields(emulated, annotatedElementPredicate);
        Set<Method> methods = ReflectionUtils.getMethods(emulated, annotatedElementPredicate);

        ClassRefsRenamer renamer = new ClassRefsRenamer(referenceCache, preimage -> {
            String newName = emulations2.get(preimage);
            if(newName != null) {
                return newName;
            }
            else {
                return preimage;
            }
        });

        Set<String> interfaces = emuCls.getInterfaces();
        Iterator<String> interfaceIt = interfaces.iterator();
        while(interfaceIt.hasNext()) {
            String interfaceStr = interfaceIt.next();
            ClassReader interfaceClassHolder = innerSource.get(interfaceStr);
            if(interfaceClassHolder != null) {
                ClassHolder classHolder = ModelUtils.copyClass(interfaceClassHolder);
                ClassHolder interfaceRename = renamer.rename(classHolder);
                String name = interfaceRename.getName();
                cls.getInterfaces().add(name);
            }
        }

//
//        if(emulatedClassName.contains("FloatBufferDirect")) {
//
//            String parent = emuCls.getParent();
//            if(parent != null && !parent.isEmpty()) {
//                ClassReader parentClassHolder = innerSource.get(parent);
//                if(parentClassHolder != null) {
//                    ClassHolder classHolder = ModelUtils.copyClass(parentClassHolder);
//                    ClassHolder parentClassHolderRename = renamer.rename(classHolder);
//                    String name = parentClassHolderRename.getName();
//                    cls.setParent(name);
//                }
//            }
//
//        }
        for(Field field : fields) {
            String emuFieldName = field.getName();
            FieldReader emulatedField = emuCls.getField(emuFieldName);
            FieldHolder originalField = cls.getField(emuFieldName);
            if(originalField != null) {
                cls.removeField(originalField);
            }
            FieldHolder fieldHolder = ModelUtils.copyField(emulatedField);
            FieldHolder fieldRename = renamer.rename(fieldHolder);
            cls.addField(fieldRename);
        }

        for(Method method : methods) {
            Class[] classes = new Class[method.getParameterTypes().length + 1];
            classes[classes.length - 1] = method.getReturnType();
            System.arraycopy(method.getParameterTypes(), 0, classes, 0, method.getParameterTypes().length);
            MethodDescriptor methodDescriptor = new MethodDescriptor(method.getName(), classes);
            MethodReader emulatedMethodReader = emuCls.getMethod(methodDescriptor);
            MethodHolder methodHolderCopy = ModelUtils.copyMethod(emulatedMethodReader);
            MethodHolder methodRename = renamer.rename(methodHolderCopy);
            MethodDescriptor descriptor = methodRename.getDescriptor();
            MethodHolder originalMethod = cls.getMethod(descriptor);
            if(originalMethod != null) {
                cls.removeMethod(originalMethod);
            }
            cls.addMethod(methodRename);
        }
    }

    private void replaceClass(ClassReaderSource innerSource, final ClassHolder cls, final ClassReader emuCls) {
        ClassRefsRenamer renamer = new ClassRefsRenamer(referenceCache, preimage -> {
            String newName = emulations2.get(preimage);
            if(newName != null) {
                return newName;
            }
            else {
                return preimage;
            }
        });

        cls.getInterfaces().clear();
        Set<String> interfaces = emuCls.getInterfaces();
        Iterator<String> interfaceIt = interfaces.iterator();
        while(interfaceIt.hasNext()) {
            String interfaceStr = interfaceIt.next();
            ClassReader interfaceClassHolder = innerSource.get(interfaceStr);
            if(interfaceClassHolder != null) {
                ClassHolder classHolder = ModelUtils.copyClass(interfaceClassHolder);
                ClassHolder interfaceRename = renamer.rename(classHolder);
                String name = interfaceRename.getName();
                cls.getInterfaces().add(name);
            }
        }

        String parent = emuCls.getParent();
        if(parent != null && !parent.isEmpty()) {
            ClassReader parentClassHolder = innerSource.get(parent);
            if(parentClassHolder != null) {
                ClassHolder classHolder = ModelUtils.copyClass(parentClassHolder);
                ClassHolder parentClassHolderRename = renamer.rename(classHolder);
                String name = parentClassHolderRename.getName();
                cls.setParent(name);
            }
        }

        for(FieldHolder field : cls.getFields().toArray(new FieldHolder[0])) {
            cls.removeField(field);
        }
        for(MethodHolder method : cls.getMethods().toArray(new MethodHolder[0])) {
            cls.removeMethod(method);
        }
        for(FieldReader field : emuCls.getFields()) {
            FieldHolder newfieldHolder = ModelUtils.copyField(field);
            FieldHolder renamedField = renamer.rename(newfieldHolder);
            cls.addField(renamedField);
        }
        for(MethodReader method : emuCls.getMethods()) {
            MethodHolder newMethodHolder = ModelUtils.copyMethod(method);
            MethodHolder renamedMethod = renamer.rename(newMethodHolder);
            cls.addMethod(renamedMethod);
        }
    }
}
