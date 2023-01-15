package max.learn

import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

class CustomCoroutineTreeNodeIterator(val root: TreeNode) {
    private var data: String? = null
    private val walker: Coroutine

    init {
        walker = Coroutine {
            treeWalk(root)
        }
    }

    suspend fun moveToNext(): Boolean {
        walker.resume()
        return !walker.isCompleted()
    }

    suspend fun get(): String? {
        if (walker.isCompleted() || !walker.isStarted()) {
            return null
        }
        var res: String? = data
        if (res == null) {
            Coroutine.suspend()
            res = data
        }
        data = null
        return res
    }

    private suspend fun treeWalk(node: TreeNode?) {
        if (node?.left != null) {
            treeWalk(node.left)
        }
        data = node?.data

        Coroutine.suspend()

        if (node?.right != null) {
            treeWalk(node.right)
        }
    }
}

fun main(): Unit = runBlocking(newSingleThreadContext("my-thread")) {

    val root = TreeNode(
        "1",
        TreeNode(
            "2",
            TreeNode("4"),
            TreeNode("5")
        ),
        TreeNode("6")
    )

    var iterator = CustomCoroutineTreeNodeIterator(root)

    while (iterator.moveToNext()) {
        println(iterator.get())
    }
}