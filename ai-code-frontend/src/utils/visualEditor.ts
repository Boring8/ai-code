/**
 * 可视化编辑器工具类
 * 处理iframe通信和元素选择逻辑
 */

export interface SelectedElement {
  tagName: string
  id: string
  className: string
  textContent: string
  outerHTML: string
  selector: string
  boundingRect: {
    top: number
    left: number
    width: number
    height: number
  }
}

export class VisualEditor {
  private iframe: HTMLIFrameElement | null = null
  private isEditMode = false
  private selectedElement: SelectedElement | null = null
  private messageListener: ((event: MessageEvent) => void) | null = null
  private onElementSelect: ((element: SelectedElement | null) => void) | null = null
  private onModeChange: ((isEditMode: boolean) => void) | null = null

  constructor() {
    this.setupMessageListener()
  }

  /**
   * 设置iframe引用
   */
  setIframe(iframe: HTMLIFrameElement) {
    this.iframe = iframe
  }

  /**
   * 设置回调函数
   */
  setCallbacks(
    onElementSelect: (element: SelectedElement | null) => void,
    onModeChange: (isEditMode: boolean) => void
  ) {
    this.onElementSelect = onElementSelect
    this.onModeChange = onModeChange
  }

  /**
   * 切换编辑模式
   */
  toggleEditMode() {
    this.isEditMode = !this.isEditMode
    this.onModeChange?.(this.isEditMode)
    
    if (this.isEditMode) {
      this.enableEditMode()
    } else {
      this.disableEditMode()
    }
  }

  /**
   * 启用编辑模式
   */
  private enableEditMode() {
    if (!this.iframe?.contentWindow) return

    // 向iframe发送启用编辑模式的消息
    this.iframe.contentWindow.postMessage({
      type: 'ENABLE_EDIT_MODE'
    }, '*')
  }

  /**
   * 禁用编辑模式
   */
  private disableEditMode() {
    if (!this.iframe?.contentWindow) return

    // 向iframe发送禁用编辑模式的消息
    this.iframe.contentWindow.postMessage({
      type: 'DISABLE_EDIT_MODE'
    }, '*')

    // 清除选中的元素
    this.clearSelection()
  }

  /**
   * 清除选中的元素
   */
  clearSelection() {
    this.selectedElement = null
    this.onElementSelect?.(null)

    if (!this.iframe?.contentWindow) return

    // 向iframe发送清除选择的消息
    this.iframe.contentWindow.postMessage({
      type: 'CLEAR_SELECTION'
    }, '*')
  }

  /**
   * 获取选中的元素
   */
  getSelectedElement(): SelectedElement | null {
    return this.selectedElement
  }

  /**
   * 获取编辑模式状态
   */
  getEditMode(): boolean {
    return this.isEditMode
  }

  /**
   * 设置消息监听器
   */
  private setupMessageListener() {
    this.messageListener = (event: MessageEvent) => {
      // 确保消息来源是iframe
      if (!this.iframe || event.source !== this.iframe.contentWindow) {
        return
      }

      const { type, data } = event.data

      switch (type) {
        case 'ELEMENT_SELECTED':
          this.selectedElement = data
          this.onElementSelect?.(data)
          break
        
        case 'ELEMENT_CLEARED':
          this.selectedElement = null
          this.onElementSelect?.(null)
          break
      }
    }

    window.addEventListener('message', this.messageListener)
  }

  /**
   * 销毁编辑器
   */
  destroy() {
    if (this.messageListener) {
      window.removeEventListener('message', this.messageListener)
      this.messageListener = null
    }
    
    this.disableEditMode()
    this.iframe = null
    this.onElementSelect = null
    this.onModeChange = null
  }

  /**
   * 生成iframe注入脚本
   * 这个脚本会被注入到iframe中，用于处理元素选择
   */
  static getInjectionScript(): string {
    return `
      (function() {
        let isEditMode = false;
        let selectedElement = null;
        let hoverElement = null;
        
        const HOVER_BORDER_STYLE = '2px solid #1890ff';
        const SELECTED_BORDER_STYLE = '3px solid #ff4d4f';
        
        // 存储原始样式
        const originalStyles = new WeakMap();
        
        // 保存元素原始样式
        function saveOriginalStyle(element) {
          if (!originalStyles.has(element)) {
            originalStyles.set(element, {
              border: element.style.border,
              outline: element.style.outline,
              cursor: element.style.cursor
            });
          }
        }
        
        // 恢复元素原始样式
        function restoreOriginalStyle(element) {
          const original = originalStyles.get(element);
          if (original) {
            element.style.border = original.border;
            element.style.outline = original.outline;
            element.style.cursor = original.cursor;
          }
        }
        
        // 应用悬浮样式
        function applyHoverStyle(element) {
          if (element === selectedElement) return;
          
          saveOriginalStyle(element);
          element.style.border = HOVER_BORDER_STYLE;
          element.style.cursor = 'pointer';
        }
        
        // 应用选中样式
        function applySelectedStyle(element) {
          saveOriginalStyle(element);
          element.style.border = SELECTED_BORDER_STYLE;
          element.style.cursor = 'pointer';
        }
        
        // 清除所有样式
        function clearAllStyles() {
          // 清除悬浮样式
          if (hoverElement) {
            restoreOriginalStyle(hoverElement);
            hoverElement = null;
          }
          
          // 清除选中样式
          if (selectedElement) {
            restoreOriginalStyle(selectedElement);
            selectedElement = null;
          }
        }
        
        // 获取元素选择器
        function getElementSelector(element) {
          if (element.id) {
            return '#' + element.id;
          }
          
          if (element.className) {
            const classes = element.className.split(' ').filter(c => c.trim());
            if (classes.length > 0) {
              return element.tagName.toLowerCase() + '.' + classes.join('.');
            }
          }
          
          // 回退到标签名
          let selector = element.tagName.toLowerCase();
          let parent = element.parentElement;
          
          if (parent) {
            const siblings = Array.from(parent.children).filter(
              child => child.tagName === element.tagName
            );
            
            if (siblings.length > 1) {
              const index = siblings.indexOf(element) + 1;
              selector += ':nth-of-type(' + index + ')';
            }
          }
          
          return selector;
        }
        
        // 创建元素信息对象
        function createElementInfo(element) {
          const rect = element.getBoundingClientRect();
          return {
            tagName: element.tagName.toLowerCase(),
            id: element.id || '',
            className: element.className || '',
            textContent: element.textContent?.trim().substring(0, 100) || '',
            outerHTML: element.outerHTML.substring(0, 500),
            selector: getElementSelector(element),
            boundingRect: {
              top: rect.top,
              left: rect.left,
              width: rect.width,
              height: rect.height
            }
          };
        }
        
        // 鼠标悬浮事件
        function handleMouseOver(event) {
          if (!isEditMode) return;
          
          event.stopPropagation();
          const element = event.target;
          
          // 跳过body和html元素
          if (element.tagName === 'BODY' || element.tagName === 'HTML') {
            return;
          }
          
          // 清除之前的悬浮样式
          if (hoverElement && hoverElement !== element) {
            restoreOriginalStyle(hoverElement);
          }
          
          hoverElement = element;
          applyHoverStyle(element);
        }
        
        // 鼠标离开事件
        function handleMouseOut(event) {
          if (!isEditMode) return;
          
          event.stopPropagation();
          const element = event.target;
          
          // 只有当鼠标真正离开元素时才清除样式
          if (!element.contains(event.relatedTarget) && element === hoverElement) {
            restoreOriginalStyle(element);
            hoverElement = null;
          }
        }
        
        // 点击事件
        function handleClick(event) {
          if (!isEditMode) return;
          
          event.preventDefault();
          event.stopPropagation();
          
          const element = event.target;
          
          // 跳过body和html元素
          if (element.tagName === 'BODY' || element.tagName === 'HTML') {
            return;
          }
          
          // 清除之前选中的元素样式
          if (selectedElement) {
            restoreOriginalStyle(selectedElement);
          }
          
          selectedElement = element;
          applySelectedStyle(element);
          
          // 向父窗口发送选中元素信息
          const elementInfo = createElementInfo(element);
          window.parent.postMessage({
            type: 'ELEMENT_SELECTED',
            data: elementInfo
          }, '*');
        }
        
        // 启用编辑模式
        function enableEditMode() {
          isEditMode = true;
          document.body.style.userSelect = 'none';
          
          // 添加事件监听器
          document.addEventListener('mouseover', handleMouseOver, true);
          document.addEventListener('mouseout', handleMouseOut, true);
          document.addEventListener('click', handleClick, true);
        }
        
        // 禁用编辑模式
        function disableEditMode() {
          isEditMode = false;
          document.body.style.userSelect = '';
          
          // 移除事件监听器
          document.removeEventListener('mouseover', handleMouseOver, true);
          document.removeEventListener('mouseout', handleMouseOut, true);
          document.removeEventListener('click', handleClick, true);
          
          // 清除所有样式
          clearAllStyles();
        }
        
        // 清除选择
        function clearSelection() {
          if (selectedElement) {
            restoreOriginalStyle(selectedElement);
            selectedElement = null;
            
            window.parent.postMessage({
              type: 'ELEMENT_CLEARED'
            }, '*');
          }
        }
        
        // 监听父窗口消息
        window.addEventListener('message', function(event) {
          const { type } = event.data;
          
          switch (type) {
            case 'ENABLE_EDIT_MODE':
              enableEditMode();
              break;
            case 'DISABLE_EDIT_MODE':
              disableEditMode();
              break;
            case 'CLEAR_SELECTION':
              clearSelection();
              break;
          }
        });
      })();
    `;
  }
}

export default VisualEditor
