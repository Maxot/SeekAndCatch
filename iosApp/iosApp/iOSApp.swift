import SwiftUI
import ComposeApp

@main
struct iOSApp: App {

    init() {
        IosKoinHelperKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
