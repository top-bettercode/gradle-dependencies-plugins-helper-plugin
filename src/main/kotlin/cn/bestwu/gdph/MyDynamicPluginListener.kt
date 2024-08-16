package cn.bestwu.gdph

/**
 *
 * @author Peter Wu
 */
import com.intellij.ide.plugins.DynamicPluginListener
import com.intellij.ide.plugins.IdeaPluginDescriptor

class MyDynamicPluginListener : DynamicPluginListener {

    override fun beforePluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        // 插件加载前的操作
        println("Plugin is about to be loaded: ${pluginDescriptor.pluginId}")
    }

    override fun pluginLoaded(pluginDescriptor: IdeaPluginDescriptor) {
        // 插件加载后的操作
        println("Plugin loaded: ${pluginDescriptor.pluginId}")
    }

    override fun beforePluginUnload(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        // 插件卸载前的操作
        println("Plugin is about to be unloaded: ${pluginDescriptor.pluginId}")
        // 例如，取消长时间运行的任务
        cancelLongRunningTasks()
    }

    override fun pluginUnloaded(pluginDescriptor: IdeaPluginDescriptor, isUpdate: Boolean) {
        // 插件卸载后的操作
        println("Plugin unloaded: ${pluginDescriptor.pluginId}")
        // 例如，清除缓存或清理 UserDataHolder 数据
        clearUserData()
    }

    private fun cancelLongRunningTasks() {
        // 实现取消长时间运行的任务
    }

    private fun clearUserData() {
        // 实现清理 UserDataHolder 数据
    }
}
