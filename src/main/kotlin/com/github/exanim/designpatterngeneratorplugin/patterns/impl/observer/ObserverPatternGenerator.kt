package com.github.exanim.designpatterngeneratorplugin.patterns.impl.observer

import com.github.exanim.designpatterngeneratorplugin.patterns.api.PatternParameter
import com.github.exanim.designpatterngeneratorplugin.patterns.api.ParameterType
import com.github.exanim.designpatterngeneratorplugin.patterns.base.BaseJavaPatternGenerator
import com.github.exanim.designpatterngeneratorplugin.patterns.base.JavaGeneratedFile

/**
 * Observer Pattern Generator
 * Generates Subject, Observer interface, concrete observers (named), optional Client.
 */
class ObserverPatternGenerator : BaseJavaPatternGenerator() {
    override val id = "observer"
    override val nameKey = "pattern.observer.name"
    override val descriptionKey = "pattern.observer.description"

    override val parameters = listOf(
        PatternParameter("packageName", "pattern.parameters.packageName", ParameterType.STRING, required = true),
        PatternParameter("subjectClassName", "pattern.parameters.subjectClassName", ParameterType.STRING, required = true, defaultValue = "Subject"),
        PatternParameter("observerInterfaceName", "pattern.parameters.observerInterfaceName", ParameterType.STRING, required = true, defaultValue = "Observer"),
        PatternParameter("firstConcreteObserverName", "pattern.parameters.firstConcreteObserverName", ParameterType.STRING, required = true),
        PatternParameter("secondConcreteObserverName", "pattern.parameters.secondConcreteObserverName", ParameterType.STRING, required = false),
        PatternParameter("clientClassName", "pattern.parameters.clientClassName", ParameterType.STRING, required = false)
    )

    override fun buildFiles(values: Map<String, Any>): List<JavaGeneratedFile> {
        val pkg = values["packageName"] as? String ?: return emptyList()
        val subject = values["subjectClassName"] as? String ?: return emptyList()
        val observerIface = values["observerInterfaceName"] as? String ?: return emptyList()
        val o1 = values["firstConcreteObserverName"] as? String ?: return emptyList()
        val o2 = (values["secondConcreteObserverName"] as? String)?.takeIf { it.isNotBlank() }
        val client = (values["clientClassName"] as? String)?.takeIf { it.isNotBlank() }

        val files = mutableListOf<JavaGeneratedFile>()

        val subjectSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("import java.util.*;")
            appendLine("/** Subject maintains list of observers and notifies on state change */")
            appendLine("public class $subject {")
            appendLine("    private final List<$observerIface> observers = new ArrayList<>();")
            appendLine("    private int state;")
            appendLine("    public void attach($observerIface o){ observers.add(o); }")
            appendLine("    public void detach($observerIface o){ observers.remove(o); }")
            appendLine("    public void setState(int s){ state=s; notifyObservers(); }")
            appendLine("    public int getState(){ return state; }")
            appendLine("    private void notifyObservers(){ for($observerIface o: observers){ o.update(this); } }")
            appendLine("}")
        }
        files += JavaGeneratedFile("$subject.java", subjectSrc, pkg)

        val observerIfaceSrc = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Observer interface */")
            appendLine("public interface $observerIface { void update($subject subject); }")
        }
        files += JavaGeneratedFile("$observerIface.java", observerIfaceSrc, pkg)

        fun concreteObserver(name: String, role: String) = buildString {
            appendLine("package $pkg;")
            appendLine()
            appendLine("/** Concrete Observer: $name ($role) */")
            appendLine("public class $name implements $observerIface {")
            appendLine("    @Override public void update($subject subject){ System.out.println(\"$name observed state=\"+subject.getState()); }")
            appendLine("}")
        }
        val allObservers = mutableListOf<String>()
        allObservers += o1
        if (o2 != null) allObservers += o2
        files += JavaGeneratedFile("$o1.java", concreteObserver(o1, "primary"), pkg)
        if (o2 != null) files += JavaGeneratedFile("$o2.java", concreteObserver(o2, "secondary"), pkg)

        client?.let { clientName ->
            val clientSrc = buildString {
                appendLine("package $pkg;")
                appendLine()
                appendLine("/** Client demonstrates observer notifications */")
                appendLine("public class $clientName {")
                appendLine("    public static void main(String[] args){")
                appendLine("        $subject subject = new $subject();")
                allObservers.forEach { on -> appendLine("        subject.attach(new $on());") }
                appendLine("        subject.setState(1);")
                appendLine("        subject.setState(2);")
                appendLine("    }")
                appendLine("}")
            }
            files += JavaGeneratedFile("$clientName.java", clientSrc, pkg)
        }

        return files
    }
}
